# Endpoints para frontend web (status atual)

Este documento lista os endpoints necessários para a UX do frontend e o que já foi implementado.

## 1) Perfil do usuario (inclui codigo de indicacao)

### Endpoint implementado

- `GET /api/me/profile?userId={uuid}[&name=...&email=...]`

### Resposta

```json
{
  "userId": "uuid",
  "name": "string",
  "email": "string",
  "referralCode": "string"
}
```

### Motivo

Permite ao frontend montar o link de indicacao (`?ref=...`) sem depender de chamada direta ao `profile-service`.

---

## 2) Saldo de moedas via BFF

### Endpoint implementado

- `GET /api/me/balance?userId={uuid}`

### Resposta

```json
{
  "userId": "uuid",
  "balance": 120
}
```

### Motivo

Evitar chamada direta do browser ao `ledger-service` (`/v1/ledger/balance/{userId}`), centralizando no BFF.

---

## 3) Resumo da campanha para home (inscritos/restantes + premios possiveis)

### Endpoint implementado

- `GET /api/campaigns/{campaignId}/summary`

### Resposta

```json
{
  "campaignId": "uuid",
  "subscribersCount": 340,
  "remainingSlots": 160,
  "possiblePrizes": [
    {
      "couponId": "uuid",
      "codePreview": "CUPOM-***",
      "priority": 10
    }
  ]
}
```

### Motivo

A home precisa mostrar:

- quantos usuarios ja se inscreveram;
- quantos faltam;
- quais os possiveis premios.

O BFF segue expondo listagem simples, mas agora também expõe `summary` por campanha.

---

## 4) Alternativa de menor round-trip (ainda opcional)

Para reduzir chamadas na home, opcionalmente criar:

- `GET /api/campaigns/home-feed`

Com lista de campanhas ja contendo:

- estado;
- datas principais;
- `subscribersCount`;
- `remainingSlots`;
- `possiblePrizes` (resumido).

---

## Observacao de seguranca/autorizacao

Caso o projeto evolua para autenticacao efetiva no BFF, endpoints `me/*` devem usar o usuario do token (sem receber `userId` por query/body).