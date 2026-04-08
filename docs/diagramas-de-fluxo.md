# Diagramas

## 1) Arquitetura (componentes)

```mermaid
flowchart LR
  UI[Frontend] -->|HTTP| BFF[BFF Service]

  BFF -->|HTTP| AUTH[auth-service]
  BFF -->|HTTP| PROFILE[profile-service]
  BFF -->|HTTP| CAMPAIGNS[campaigns-service]
  BFF -->|HTTP| LEDGER[ledger-service]
  BFF -->|HTTP| PRIZES[prizes-service]

  CAMPAIGNS -->|producer| KAFKA[Kafka]
  PROFILE -->|producer| KAFKA
  KAFKA -->|consumer| PRIZES
  KAFKA -->|consumer| LEDGER
  KAFKA -->|consumer| CAMPAIGNS

  AUTH --> MYSQL_AUTH[(MySQL auth)]
  PROFILE --> MYSQL_PROFILE[(MySQL profile)]
  CAMPAIGNS --> MYSQL_CAMPAIGNS[(MySQL campaigns)]
  LEDGER --> MYSQL_LEDGER[(MySQL ledger)]
  PRIZES --> MYSQL_PRIZES[(MySQL prizes)]
```

**Tópicos Kafka relevantes (nomes por defeito):** `campaign.subscription.debit.request` → ledger; `campaign.subscription.payment.succeeded` / `campaign.subscription.payment.failed` → campaigns; `prize.distribution.request` → prizes; `referral.bonus.granted` (profile → ledger, bónus de indicação no registo).

---

## 2) Borda HTTP: Resource, mapper e serviços (por microsserviço)

```mermaid
flowchart TB
  subgraph borda [infra — borda HTTP]
    R[Resource / Controller]
    M[RestMapper — DTO para entidade e resposta]
    DTO[DTOs em infra.resource.dto]
  end
  subgraph app [domain — casos de uso]
    S[Serviços em domain.service]
    E[Entidades em domain.entity]
  end
  subgraph infra2 [infra — técnico]
    REPO[persistence — repositórios]
    MSG[messaging — Kafka]
    GW[client / gateway HTTP]
  end

  DTO --> R
  R --> M
  M -->|entidade montada| S
  S --> E
  S --> REPO
  S --> MSG
  S --> GW
  S -->|entidade| M
  M -->|DTO de resposta| R
```

Os serviços **não** referenciam DTOs de API; quem converte pedido/resposta é o **mapper** na mesma camada que o `Resource`.

---

## 3) Registro no auth cria profile (E2E síncrono)

```mermaid
sequenceDiagram
  participant U as Usuário
  participant AR as auth-service Resource
  participant AM as AuthRestMapper
  participant AS as AuthRegistrationService
  participant P as profile-service

  U->>AR: POST /v1/auth/register (JSON)
  AR->>AM: toUser(RegisterRequest)
  AM-->>AR: User (sem passwordHash)
  AR->>AS: register(User, rawPassword)
  AS->>AS: hash, salvar User (DB auth)
  AS->>P: POST /v1/profiles (criar Profile do userId)
  alt Profile já existe
    P-->>AS: 409 CONFLICT
    AS-->>AR: User persistido
    AR-->>U: 201 CREATED (token JWT emitido no Resource)
  else Profile criado com sucesso
    P-->>AS: 201 CREATED
    AS-->>AR: User
    AR-->>U: 201 CREATED (token)
  else Falha ao comunicar/criar Profile
    P-->>AS: 4xx/5xx/timeout
    AS-->>AR: exceção → rollback @Transactional
    AR-->>U: 502 BAD_GATEWAY
  end
```

**Indicação (`referralCode`):** quando presente no registo, o `profile-service` valida de forma **síncrona** (código existente, ainda não usado, não auto-indicação); o detalhe está na **secção 4**. O bónus em pontos é **assíncrono** via Kafka (`referral.bonus.granted`).

---

## 4) Registo com código de indicação: validação no profile + bónus no ledger

Fluxo quando o utilizador envia `referralCode` no `POST /v1/auth/register` (ou BFF `/api/auth/register`). A validação e a gravação da redenção (`referral_redemptions`) ocorrem **na mesma transação** do perfil; a mensagem Kafka só é enviada **após commit** (`@TransactionalEventListener`).

```mermaid
sequenceDiagram
  participant U as Utilizador
  participant A as auth-service
  participant P as profile-service
  participant K as Kafka
  participant L as ledger-service

  U->>A: POST register (referralCode opcional)
  A->>A: hash password, salvar User
  A->>P: POST /v1/profiles (userId, displayName, referralCode?)
  alt referralCode inválido ou já usado
    P-->>A: 400 + error
    A-->>U: 400 (rollback — User não fica criado)
  else referralCode válido
    P->>P: perfil + redemption (transação)
    P-->>A: 201
    Note over P,K: após commit
    P->>K: referral.bonus.granted (newUserId, referrerUserId, …)
    K->>L: consumir
    L->>L: credit indicado + credit indicador (10+10, idempotência)
  else sem código
    P->>P: só criar perfil + gerar código próprio
    P-->>A: 201
  end
  A-->>U: 201 + JWT (se passou no profile)
```

---

## 5) Inscrição na campanha: débito assíncrono (Kafka) e estado da subscrição

```mermaid
sequenceDiagram
  participant U as Usuário
  participant CR as campaigns Resource
  participant CS as CampaignSubscriptionService
  participant K as Kafka
  participant L as ledger-service consumer
  participant LS as LedgerDebitService
  participant CC as campaigns consumers

  U->>CR: POST /v1/campaigns/{id}/subscriptions (userId)
  CR->>CS: subscribe(campaignId, userId)
  CS->>CS: gravar subscrição PROCESSING (transação)
  CS->>K: após commit — campaign.subscription.debit.request
  CR-->>U: 204 No Content

  K->>L: pedido de débito
  L->>LS: debit(LedgerEntry)
  alt saldo OK
    LS->>K: campaign.subscription.payment.succeeded
  else saldo insuficiente / erro
    LS->>K: campaign.subscription.payment.failed
  end
  K->>CC: payment.succeeded / payment.failed
  CC->>CC: atualizar subscrição para ACTIVE ou PAYMENT_FAILED
```

---

## 6) Distribuição de prémio (agendador + Kafka) e consulta no prizes

```mermaid
sequenceDiagram
  participant SCH as CampaignDistributionScheduler
  participant CAS as CampaignAllocationService
  participant K as Kafka
  participant PR as prizes-service consumer
  participant U as Usuário

  Note over SCH: uled — campanhas ACTIVE com distributionAt vencido
  SCH->>CAS: allocate(campaignId, userId) por subscrição ACTIVE
  CAS->>CAS: CampaignAllocation + cupom ASSIGNED (transação REQUIRES_NEW)
  CAS->>K: prize.distribution.request
  K->>PR: consumir evento
  PR->>PR: deduplicar + PrizeDelivery DELIVERED

  U->>PR: GET /v1/prizes/users/{userId}?campaignId=...
  Note over PR: Resource + PrizeDeliveryRestMapper → DTO
```

**Retry:** `PrizeDispatchRetryService` consulta `PrizesGateway` e pode republicar no mesmo tópico quando o prémio ainda não está confirmado como entregue.

---

## 7) Crédito / débito direto no ledger (REST)

API exposta pelo `ledger-service` **sem passar pelo BFF**. Não faz parte do fluxo “utilizador → BFF” nem substitui os débitos/créditos assíncronos (Kafka) das subscrições ou do bónus de indicação.

**Para que serve hoje:** sobretudo **testes de integração** (`coupons-it`), que creditam pontos via `POST /v1/ledger/credit` antes de subscrever campanhas — no produto não existe outro endpoint público para “carregar” saldo. Também permite **ajustes manuais** (admin/ferramentas) se alguém chamar o ledger diretamente.

**Remover estes endpoints** implicaria alterar os testes de integração (ou outra forma de injetar saldo).

```mermaid
sequenceDiagram
  participant U as Cliente HTTP
  participant LR as ledger Resource
  participant LM as LedgerRestMapper
  participant LC as LedgerCreditService / LedgerDebitService
  participant LP as LedgerPostingService

  U->>LR: POST /v1/ledger/credit ou /debit (EntryRequest JSON)
  LR->>LM: toLedgerLine(EntryRequest)
  LM-->>LR: LedgerEntry (valor absoluto em amount)
  LR->>LC: credit(line) ou debit(line)
  LC->>LP: postWithSignedAmount(line, ±amount)
  LP-->>LC: LedgerEntry persistido (idempotência por chave)
  LC-->>LR: LedgerEntry
  LR->>LM: toResponse(LedgerEntry)
  LR-->>U: EntryResponse JSON
```

---

*Diagramas alinhados ao código atual (Resource + RestMapper, Kafka para subscrição/débito, indicação no registo, schedulers de campanhas).*
