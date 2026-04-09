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
  BFF -->|HTTP| CHEST[daily-chest-service]

  CAMPAIGNS -->|producer| KAFKA[Kafka]
  PROFILE -->|producer| KAFKA
  CHEST -->|producer| KAFKA
  KAFKA -->|consumer| PRIZES
  KAFKA -->|consumer| LEDGER
  KAFKA -->|consumer| CAMPAIGNS

  AUTH --> MYSQL_AUTH[(MySQL auth)]
  PROFILE --> MYSQL_PROFILE[(MySQL profile)]
  CAMPAIGNS --> MYSQL_CAMPAIGNS[(MySQL campaigns)]
  LEDGER --> MYSQL_LEDGER[(MySQL ledger)]
  PRIZES --> MYSQL_PRIZES[(MySQL prizes)]
  CHEST --> MYSQL_CHEST[(MySQL daily_chest)]
```

**Tópicos Kafka relevantes (nomes por defeito):** `campaign.subscription.debit.request` → ledger; `campaign.subscription.payment.succeeded` / `campaign.subscription.payment.failed` → campaigns; `prize.distribution.request` → prizes; `referral.bonus.granted` (profile → ledger, bónus de indicação no registo); `chest.bonus.granted` (daily-chest → ledger, crédito diário do baú).

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

## 8) Baú da Sorte diário: abertura idempotente + crédito assíncrono no ledger

Fluxo implementado:
- frontend chama BFF (`/api/daily-chest/today` e `/api/daily-chest/open`);
- BFF apenas faz proxy para `daily-chest-service`;
- `daily-chest-service` resolve timezone via `profile-service` (fallback `America/Sao_Paulo`);
- garante abertura única por `(user_id, local_date)` e publica evento `chest.bonus.granted`;
- `ledger-service` consome e credita com `reason=DAILY_CHEST_BONUS`, `refType=DAILY_CHEST`, `refId=localDate`.

```mermaid
sequenceDiagram
  participant U as Utilizador (web-app)
  participant B as bff-service
  participant C as daily-chest-service
  participant P as profile-service
  participant K as Kafka
  participant L as ledger-service

  U->>B: GET /api/daily-chest/today?userId=...
  B->>C: GET /v1/daily-chest/today?userId=...
  C->>P: GET /v1/profiles/{userId}
  C->>C: timezone + localDate (fallback SP)
  alt já abriu hoje
    C-->>B: 200 openedToday=true + rewardCoins + openedAt
  else ainda não abriu
    C-->>B: 200 openedToday=false
  end
  B-->>U: resposta today

  U->>B: POST /api/daily-chest/open {userId}
  B->>C: POST /v1/daily-chest/open {userId}
  C->>P: GET /v1/profiles/{userId}
  C->>C: verificar UNIQUE(user_id, local_date)
  alt já abriu hoje
    C-->>B: 200 alreadyOpened=true + mesmo prémio
  else primeira abertura do dia
    C->>C: sortear 10/50/100 (80/15/5), persistir
    C->>K: chest.bonus.granted (userId, rewardCoins, localDate, idempotencyKey)
    C-->>B: 200 alreadyOpened=false + prémio
    K->>L: consumir evento
    L->>L: creditar idempotente no ledger
  end
  B-->>U: resposta open
```

---

## 9) Frontend autenticado: FAB do Baú da Sorte

No `web-app`, o `DailyChestFab` foi integrado no `AuthenticatedLayout` (aparece em todas as páginas autenticadas, acima do bottom nav):
- estado inicial via `GET /api/daily-chest/today`;
- ao abrir: animação local de suspense (1.5s–2.5s) + `POST /api/daily-chest/open`;
- após sucesso: mostra resultado e dispara evento `coupons:balance-refresh` para atualizar saldo em `Home` e `Conta`.

```mermaid
flowchart LR
  L[AuthenticatedLayout] --> FAB[DailyChestFab]
  FAB -->|GET today| BFF[/api/daily-chest/today]
  FAB -->|POST open| BFF2[/api/daily-chest/open]
  FAB -->|window event| EVT[coupons:balance-refresh]
  EVT --> HOME[HomePage recarrega saldo]
  EVT --> ACCOUNT[AccountPage recarrega saldo]
```

---

## 10) Autenticação e autorização (web-app → BFF → auth-service)

**Princípio:** o **browser** fala só com o **BFF**. O JWT é emitido pelo **auth-service** e **validado no BFF** (mesma chave HMAC `JWT_SECRET`). O `userId` em operações “minhas” vem do **`sub` do token**, não de parâmetros confiáveis do cliente.

### 10.1) Login e uso do token no frontend

```mermaid
sequenceDiagram
  participant W as web-app
  participant B as bff-service
  participant A as auth-service

  W->>B: POST /api/auth/register ou /api/auth/login (sem Bearer)
  B->>A: POST /v1/auth/register ou /v1/auth/login
  A->>A: User + role (USER por defeito; ADMIN na BD)
  A-->>B: AuthResponse (token, userId, email, name, roles[])
  B-->>W: AuthTokenResponse (mesmo formato)

  Note over W: localStorage: token + userId + roles (UI)

  W->>B: GET /api/me/balance (Authorization: Bearer JWT)
  B->>B: filtro JWT — assinatura, exp, claims
  alt token válido
    B->>B: SecurityContext com userId = sub + ROLE_*
    B->>B: MeProxyResource usa utilizador autenticado
    B-->>W: 200
  else sem token / inválido / expirado
    B-->>W: 401 Não autenticado
  end
```

### 10.2) Classificação das rotas no BFF (política)

```mermaid
flowchart TB
  REQ[Pedido HTTP ao BFF]
  F[JwtAuthenticationFilter]
  ANON[Rotas permitAll — anónimo OK]
  AUTH[authenticated — utilizador com JWT válido]
  ADM[hasRole ADMIN]
  E401[401 token inválido ou ausente onde obrigatório]
  E403[403 sem permissão]

  REQ --> F
  F -->|Bearer presente e válido| CTX[SecurityContext: userId + authorities]
  F -->|Bearer inválido| E401
  CTX --> DEC{authorizeRequests}
  DEC --> ANON
  DEC --> AUTH
  DEC --> ADM
  AUTH -->|acesso a rota só ADMIN| E403

  subgraph public [Públicas — exemplos]
    P1["POST /api/auth/register e login"]
    P2["GET /api/campaigns, /{id}, /summary, /winners"]
    P3["GET /api/uploads/images/*"]
  end

  subgraph userR [Utilizador autenticado — exemplos]
    U1["/api/me/* , daily-chest/*"]
    U2["POST /api/campaigns/{id}/subscriptions"]
    U3["GET …/subscriptions/me , GET /api/prizes/me"]
  end

  subgraph adminR [Só ADMIN — exemplos]
    A1["POST ou PATCH /api/campaigns e cupons na campanha"]
    A2["/api/coupons , /companies , POST /uploads"]
    A3["/api/admin/*"]
  end

  ANON -.-> public
  AUTH -.-> userR
  ADM -.-> adminR
```

### 10.3) Conteúdo do JWT (auth-service)

| Claim / campo | Uso |
|---------------|-----|
| `sub` | UUID do utilizador (fonte de verdade no BFF) |
| `email` | E-mail |
| `roles` | Lista com `USER` e/ou `ADMIN` (Spring usa `ROLE_USER`, `ROLE_ADMIN`) |
| `exp` / `iat` | Validade e emissão |

**Bootstrap de admin:** variável `AUTH_BOOTSTRAP_ADMIN_EMAILS` (auth-service) promove contas existentes a `ADMIN` ao arranque; na BD a coluna `users.role` guarda `USER` ou `ADMIN`.

---

*Diagramas alinhados ao código atual (Resource + RestMapper, Kafka para subscrição/débito, indicação no registo, Baú da Sorte diário com crédito assíncrono no ledger, FAB no frontend autenticado, e JWT + roles no BFF).*
