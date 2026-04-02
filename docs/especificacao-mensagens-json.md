# Especificação das mensagens JSON (HTTP e Kafka)

Este documento descreve os corpos JSON usados nas APIs REST expostas pelos serviços e nas mensagens publicadas/consumidas nos tópicos Kafka. Os nomes dos campos seguem o **camelCase** padrão do Jackson (Spring Boot). **UUID** e **Instant** aparecem em JSON como strings no formato usual (UUID textual e ISO-8601 em UTC, por exemplo `2026-04-01T12:00:00Z`).

As portas indicadas são as de `application.yml` por defeito; podem ser alteradas por variáveis de ambiente.

---

## 1. APIs HTTP

### 1.1 BFF (`bff-service`, porta `8090`)

Entrada recomendada para clientes externos. Prefixo base: sem path global extra além dos indicados.

| Método | Caminho | Corpo de pedido | Resposta de sucesso |
|--------|---------|-----------------|---------------------|
| `POST` | `/api/auth/register` | [RegisterRequest](#registerrequest--loginrequest-auth--bff) | `201` — [AuthTokenResponse](#authtokenresponse-bff) |
| `POST` | `/api/auth/login` | [LoginRequest](#registerrequest--loginrequest-auth--bff) | `200` — [AuthTokenResponse](#authtokenresponse-bff) |
| `POST` | `/api/campaigns` | [CreateCampaignRequest](#createcampaignrequest) | `201` — [CampaignResponse](#campaignresponse) |
| `GET` | `/api/campaigns` | — | `200` — array de [CampaignResponse](#campaignresponse) |
| `POST` | `/api/campaigns/{campaignId}/coupons` | [AddCouponToCampaignRequest](#addcoupontocampaignrequest) | `200` — [CampaignResponse](#campaignresponse) |
| `POST` | `/api/campaigns/{campaignId}/subscriptions` | [UserIdRequest](#useridrequest) | `204` — sem corpo |
| `GET` | `/api/prizes/users/{userId}?campaignId=` (opcional) | — | `200` — array de [PrizeDeliveryResponse](#prizedeliveryresponse) |

**Nota:** O BFF **não** expõe `POST /.../allocations`; essa operação existe apenas no `campaigns-service` (ver abaixo).

#### AuthTokenResponse (BFF)

```json
{
  "token": "string (JWT)",
  "userId": "uuid",
  "email": "string",
  "name": "string"
}
```

---

### 1.2 Auth (`auth-service`, porta `8081`)

Base: `/v1/auth`

| Método | Caminho | Corpo | Resposta |
|--------|---------|-------|----------|
| `POST` | `/v1/auth/register` | [RegisterRequest](#registerrequest--loginrequest-auth--bff) | `201` — [AuthResponse](#authresponse-auth-service) |
| `POST` | `/v1/auth/login` | [LoginRequest](#registerrequest--loginrequest-auth--bff) | `200` — [AuthResponse](#authresponse-auth-service) |

#### RegisterRequest / LoginRequest (auth + BFF)

**Registo:**

```json
{
  "email": "string",
  "password": "string",
  "name": "string",
  "referralCode": "string (opcional)"
}
```

**Login:**

```json
{
  "email": "string",
  "password": "string"
}
```

#### AuthResponse (auth-service)

```json
{
  "token": "string",
  "userId": "uuid",
  "email": "string",
  "name": "string"
}
```

---

### 1.3 Profile (`profile-service`, porta `8082`)

Base: `/v1/profiles`

| Método | Caminho | Corpo | Resposta |
|--------|---------|-------|----------|
| `POST` | `/v1/profiles` | [CreateProfileRequest](#createprofilerequest) | `201` — [ProfileResponse](#profileresponse) |
| `GET` | `/v1/profiles/{userId}` | — | `200` — [ProfileResponse](#profileresponse) |
| `PUT` | `/v1/profiles/{userId}` | [UpdateProfileRequest](#updateprofilerequest) | `200` — [ProfileResponse](#profileresponse) |

#### CreateProfileRequest

```json
{
  "userId": "uuid",
  "displayName": "string",
  "timezone": "string (opcional)"
}
```

#### UpdateProfileRequest

```json
{
  "displayName": "string (opcional)",
  "timezone": "string (opcional)"
}
```

#### ProfileResponse

```json
{
  "userId": "uuid",
  "displayName": "string",
  "referralCode": "string",
  "timezone": "string",
  "createdAt": "instant"
}
```

---

### 1.4 Campaigns (`campaigns-service`, porta `8083`)

Base: `/v1/campaigns`

| Método | Caminho | Corpo | Resposta |
|--------|---------|-------|----------|
| `POST` | `/v1/campaigns` | [CreateCampaignRequest](#createcampaignrequest) | `201` — [CampaignResponse](#campaignresponse) |
| `GET` | `/v1/campaigns` | — | `200` — array de [CampaignResponse](#campaignresponse) |
| `POST` | `/v1/campaigns/{campaignId}/coupons` | [AddCouponToCampaignRequest](#addcoupontocampaignrequest) | `200` — [CampaignResponse](#campaignresponse) |
| `POST` | `/v1/campaigns/{campaignId}/subscriptions` | [UserIdRequest](#useridrequest) | `204` — sem corpo |
| `POST` | `/v1/campaigns/{campaignId}/allocations` | [UserIdRequest](#useridrequest) | `200` — [AllocationResponse](#allocationresponse) |

#### CreateCampaignRequest

```json
{
  "title": "string",
  "subscriptionsStartAt": "instant",
  "subscriptionsEndAt": "instant",
  "distributionAt": "instant",
  "pointsCost": 0
}
```

#### AddCouponToCampaignRequest

```json
{
  "code": "string",
  "expiresAt": "instant",
  "priority": 0
}
```

`priority` é opcional no modelo (pode ser omitido ou `null`).

#### UserIdRequest

```json
{
  "userId": "uuid"
}
```

#### CampaignResponse

Campos comuns ao BFF e ao serviço de campanhas. `status` é enum textual: `ACTIVE` ou `CLOSED`.

```json
{
  "id": "uuid",
  "title": "string",
  "subscriptionsStartAt": "instant",
  "subscriptionsEndAt": "instant",
  "distributionAt": "instant",
  "status": "ACTIVE | CLOSED",
  "pointsCost": 0,
  "createdAt": "instant",
  "updatedAt": "instant"
}
```

#### AllocationResponse

```json
{
  "id": "uuid",
  "campaignId": "uuid",
  "userId": "uuid",
  "couponId": "uuid",
  "codeSnapshot": "string",
  "allocatedAt": "instant"
}
```

---

### 1.5 Ledger (`ledger-service`, porta `8084`)

Base: `/v1/ledger`

| Método | Caminho | Corpo | Resposta |
|--------|---------|-------|----------|
| `POST` | `/v1/ledger/credit` | [EntryRequest](#entryrequest--entryresponse) | `201` — [EntryResponse](#entryrequest--entryresponse) |
| `POST` | `/v1/ledger/debit` | [EntryRequest](#entryrequest--entryresponse) | `201` — [EntryResponse](#entryrequest--entryresponse) |
| `GET` | `/v1/ledger/balance/{userId}` | — | `200` — [BalanceResponse](#balanceresponse) |

#### EntryRequest / EntryResponse

**Pedido:**

```json
{
  "userId": "uuid",
  "amount": 1,
  "reason": "string",
  "refType": "string (opcional)",
  "refId": "string (opcional)",
  "idempotencyKey": "string"
}
```

**Resposta:**

```json
{
  "id": "uuid",
  "userId": "uuid",
  "amount": 0,
  "reason": "string",
  "refType": "string",
  "refId": "string",
  "idempotencyKey": "string",
  "createdAt": "instant"
}
```

#### BalanceResponse

```json
{
  "userId": "uuid",
  "balance": 0
}
```

---

### 1.6 Prizes (`prizes-service`, porta `8085`)

Base: `/v1/prizes`

| Método | Caminho | Resposta |
|--------|---------|----------|
| `GET` | `/v1/prizes/users/{userId}?campaignId=` (opcional, UUID) | `200` — array de [PrizeDeliveryResponse](#prizedeliveryresponse) |

#### PrizeDeliveryResponse

```json
{
  "id": "uuid",
  "campaignId": "uuid",
  "userId": "uuid",
  "couponId": "uuid",
  "couponCode": "string",
  "status": "string",
  "processedAt": "instant"
}
```

---

### 1.7 Erros HTTP (formato habitual)

Em vários serviços, erros podem devolver JSON com um único campo:

```json
{
  "error": "string (mensagem)"
}
```

O código HTTP e o texto exato dependem do caso (validação, 404, 409, etc.).

---

### 1.8 Chamadas HTTP internas (microsserviço → microsserviço)

Não são expostas pelo BFF, mas usam o mesmo estilo JSON:

- **auth → profile:** criação de perfil no registo — corpo alinhado com [CreateProfileRequest](#createprofilerequest) / resposta alinhada com [ProfileResponse](#profileresponse) (o auth-service usa DTOs espelho `ProfileCreateRequest` e `ProfileRemoteResponse`).
- **campaigns → ledger:** crédito/débito — mesmo contrato que [EntryRequest](#entryrequest--entryresponse) / [EntryResponse](#entryrequest--entryresponse) (`LedgerEntryRemoteResponse` no cliente).

---

## 2. Mensagens Kafka

Valor da mensagem: **string JSON** (UTF-8). Chave (`key`): string; quando é UUID, usa-se a representação textual do UUID.

Os nomes dos tópicos podem ser sobrescritos por variáveis de ambiente; entre parêntesis está o **default** em `application.yml`.

### 2.1 Resumo dos tópicos

| Tópico (default) | Produtor(es) | Consumidor(es) |
|------------------|--------------|----------------|
| `campaign.subscription.debit.request` | `campaigns-service` | `ledger-service` |
| `campaign.subscription.payment.succeeded` | `ledger-service` | `campaigns-service` |
| `campaign.subscription.payment.failed` | `ledger-service` | `campaigns-service` |
| `prize.distribution.request` | `campaigns-service` | `prizes-service` |

### 2.2 `campaign.subscription.debit.request`

- **Chave:** `subscriptionId` (UUID como string).
- **Payload:**

```json
{
  "subscriptionId": "uuid",
  "campaignId": "uuid",
  "userId": "uuid",
  "amount": 0,
  "idempotencyKey": "string",
  "schemaVersion": 1
}
```

`schemaVersion` tem valor por defeito `1` no modelo Java.

### 2.3 `campaign.subscription.payment.succeeded`

- **Chave:** `subscriptionId` (UUID como string).
- **Payload:**

```json
{
  "subscriptionId": "uuid",
  "campaignId": "uuid",
  "userId": "uuid",
  "ledgerEntryId": "uuid",
  "schemaVersion": 1
}
```

### 2.4 `campaign.subscription.payment.failed`

- **Chave:** `subscriptionId` (UUID como string).
- **Payload:**

```json
{
  "subscriptionId": "uuid",
  "campaignId": "uuid",
  "userId": "uuid",
  "error": "string",
  "schemaVersion": 1
}
```

### 2.5 `prize.distribution.request`

- **Chave:** composta `"{campaignId}:{userId}:{couponId}"` (UUIDs separados por `:`).
- **Payload:**

```json
{
  "campaignId": "uuid",
  "userId": "uuid",
  "couponId": "uuid",
  "couponCode": "string",
  "occurredAt": "instant",
  "schemaVersion": 1
}
```

Na publicação atual a partir da alocação, `schemaVersion` é definido como `1`.

---

## 3. Referência rápida no código

| Área | Pacotes / ficheiros típicos |
|------|-----------------------------|
| BFF DTOs | `bff-service/.../bff/infra/resource/dto/` |
| REST resources | `*Resource.java` em cada serviço |
| Kafka DTOs | `*/infra/messaging/dto/` |
| Nomes dos tópicos | `coupons.kafka.*` em `application.yml` |

Alterações a estes contratos devem refletir-se neste ficheiro e implicam coordenação entre produtores e consumidores (especialmente Kafka).
