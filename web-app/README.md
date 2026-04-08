# Frontend Web (React)

Frontend mobile-first para o `coupons-app`.

## Como executar

```bash
cd web-app
npm install
npm run dev
```

App por defeito em `http://localhost:5173`.

## Variáveis de ambiente (opcionais)

- `VITE_BFF_URL` (default: `http://localhost:8090`)
- `VITE_LEDGER_URL` (default: `http://localhost:8084`)

## Identidade visual

O `styles.css` segue o **mesmo conjunto de tokens** do projeto de referência `lorie-web` (`src/commons/colors.css`): cinzas, rosa primário (`#FF627D`), feedback e superfícies (`#F5F7FB`, `#FEFEFF`). Tipografia **Avenir** (Roman / Book / Heavy): ficheiros copiados de `lorie-web/src/fonts/` para `web-app/src/fonts/` apenas para manter paridade visual no browser.

## Funcionalidades implementadas

- Login
- Registro simplificado com `referralCode` opcional
- Leitura automática de `?ref=...` na URL para pré-preencher indicação
- Home com listagem de campanhas em carrossel horizontal
- Ordenação de campanhas com abertas primeiro
- Inscrição na campanha
- Contador regressivo até distribuição
- Consulta de prêmio por campanha após início da distribuição
- Mensagem de “não foi dessa vez...” quando não há prêmio
- Exibição de saldo de moedas (via BFF `/api/me/balance`)

## Limitações atuais do backend para UX completa

Ainda faltam endpoints no BFF para:

- obter `referralCode` do usuário autenticado (para gerar link de indicação sem chamar profile direto);
- listar “prêmios possíveis” de uma campanha;
- retornar métrica de inscritos e vagas restantes por campanha;
- expor saldo no BFF (evitar chamada direta do frontend ao ledger).

Proposta detalhada em `docs/frontend-endpoints-proposta.md`.
