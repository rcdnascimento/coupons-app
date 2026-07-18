# Coupons App

Plataforma de **campanhas de cupons** com **pontos de fidelidade**, construída como **sistema distribuído** (disciplina de Sistemas Distribuídos).

Arquitetura: microsserviços Spring Boot + BFF + frontend React, MySQL (um schema por serviço), Apache Kafka, Prometheus e Grafana.

---

## Requisitos

### Obrigatórios (para rodar com Docker)

| Ferramenta | Versão sugerida | Como conferir |
| --- | --- | --- |
| **Git** | qualquer recente | `git --version` |
| **Docker** | 24+ | `docker --version` |
| **Docker Compose** | v2 (`docker compose`) | `docker compose version` |

Instale o [Docker Desktop](https://www.docker.com/products/docker-desktop/) (macOS / Windows) ou Docker Engine + plugin Compose (Linux).

**Deixe o Docker Desktop aberto** antes de subir a stack.

### Hardware sugerido

| Recurso | Mínimo | Recomendado |
| --- | --- | --- |
| RAM | 8 GB | 16 GB |
| Disco livre | ~10 GB | 15 GB+ |
| CPU | 2 núcleos | 4 núcleos |

A **primeira** subida baixa imagens e compila os serviços Java — pode levar **5–15 minutos**.

### Opcionais (só para desenvolvimento fora do Docker)

| Ferramenta | Versão | Uso |
| --- | --- | --- |
| JDK | 11 | rodar serviços Java localmente |
| Node.js | 18+ | frontend com Vite |
| Go | 1.21+ | testes em `integration/` |

Para a demonstração / correção da disciplina, **basta Docker**.

---

## Subir o sistema (caminho feliz)

Na raiz do repositório:

```bash
# 1. Entrar na pasta do projeto
cd coupons-app

# 2. Criar o arquivo de ambiente (valores de desenvolvimento prontos)
cp .env.example .env

# 3. Subir toda a stack (build + start)
docker compose up --build -d
```

Acompanhar a subida:

```bash
docker compose ps
docker compose logs -f
```

Espere até os serviços principais ficarem `healthy` (cerca de 1–2 minutos após o build).

### Verificar se está no ar

```bash
curl -s http://localhost:8090/actuator/health
curl -s -o /dev/null -w "web-app HTTP %{http_code}\n" http://localhost:3000/
```

Respostas esperadas: BFF com `"status":"UP"` e web-app com `HTTP 200`.

### Abrir no navegador

| URL | O que é |
| --- | --- |
| **http://localhost:3000** | Aplicação web |
| http://localhost:8088 | Kafka UI (tópicos / mensagens) |
| http://localhost:8090/actuator/health | Health do BFF |
| http://localhost:3001 | Grafana (usuário `admin` / senha em `GRAFANA_ADMIN_PASSWORD`, padrão `admin`) |
| http://localhost:9090 | Prometheus |
| **http://localhost:16686** | **Jaeger UI** (tracing distribuído) |

---

## Login de administrador (já criado no arranque)

Ao subir o `auth-service`, um usuário **ADMIN** é criado automaticamente (seed idempotente):

| Campo | Valor padrão |
| --- | --- |
| E-mail | `admin@coupons.local` |
| Senha | `admin123` |
| Nome | `Admin` |

1. Abra http://localhost:3000  
2. Faça **login** com as credenciais acima  
3. Acesse **/admin** para gerir campanhas / créditos  

Credenciais configuráveis no `.env`: `AUTH_BOOTSTRAP_ADMIN_*`.

### Utilizadores comuns (também no arranque)

| E-mail | Senha | Nome | Papel |
| --- | --- | --- | --- |
| `richard@gmail.com` | `password` | Richard | USER |
| `lucas@gmail.com` | `password` | Lucas | USER |

Desligar: `AUTH_BOOTSTRAP_DEMO_USERS_ENABLED=false`.

---

## Dados de demonstração (já prontos na subida)

Ao subir o `campaigns-service`, o sistema cria:

| Item | Conteúdo |
| --- | --- |
| **Empresas** | Uber e iFood (logos da pasta `seed-uploads`) |
| **Campanhas** | *Até R$ 50,00 na Uber* e *Até R$ 50,00 no iFood* (1 por empresa) |
| **Prêmios** | 2 cupons por campanha: **R$ 50** e **R$ 20** |

Imagens usadas (em `bff-service/src/main/resources/seed-uploads/`):

- `uber-logo.png`, `uber-campaign-background.png`
- `ifood-logo.jpg`, `ifood-campaign-background.jpg`

Campanhas abertas (~14 dias), custo padrão **10 pontos**. Desligar: `CAMPAIGNS_DEMO_SEED_ENABLED=false`.

---

## Tracing distribuído (Jaeger + OpenTelemetry)

Os microsserviços Java sobem com o **OpenTelemetry Java Agent** e exportam traces para o **Jaeger** (OTLP).

### Abrir a UI

**http://localhost:16686**

### Como ver um fluxo completo

1. Com a stack no ar, use o app (ex.: login, inscrição em campanha, baú).  
2. No Jaeger: escolha o serviço (`bff-service`, `campaigns-service`, …) → **Find Traces**.  
3. Abra um trace: você verá a timeline de spans (HTTP síncrono e, quando aplicável, Kafka).

Exemplos úteis para a disciplina:

| Ação no app | O que procurar no Jaeger |
| --- | --- |
| Login / cadastro | `bff-service` → `auth-service` (+ `profile-service` no cadastro) |
| Inscrição na campanha | `bff` → `campaigns` → (Kafka) → `ledger` → (Kafka) → `campaigns` |
| Baú diário | `bff` → `daily-chest` → `profile` + Kafka → `ledger` |

No Grafana (`:3001`) também há datasource **Jaeger** provisionado (além do Prometheus).

> É preciso **rebuild** das imagens Java após puxar estas mudanças (`docker compose up --build -d`), pois o agent entra no `Dockerfile`.

---

## O que sobe com o Compose

### Infraestrutura

- MySQL 8 (`localhost:3307`)
- Zookeeper + Kafka (`localhost:9092`)
- Kafka UI (`localhost:8088`)
- Prometheus + Grafana

### Aplicação

| Serviço | Porta |
| --- | --- |
| web-app | 3000 |
| bff-service | 8090 |
| auth-service | 8081 |
| profile-service | 8082 |
| campaigns-service | 8083 |
| ledger-service | 8084 |
| prizes-service | 8085 |
| daily-chest-service | 8086 |

O frontend fala **somente** com o BFF. Cada microsserviço tem banco próprio. Fluxos como inscrição, bônus e distribuição de prêmios usam **Kafka** (comunicação assíncrona).

---

## Parar / zerar / subir de novo

```bash
# Parar (mantém dados do MySQL)
docker compose down

# Parar e APAGAR o banco (recomeça do zero — schemas + admin seed de novo)
docker compose down -v

# Subir novamente (sem rebuild, se as imagens já existem)
docker compose up -d

# Subir com rebuild (após alterar código Java/frontend)
docker compose up --build -d
```

---

## Problemas comuns

| Sintoma | O que fazer |
| --- | --- |
| Porta em uso | Altere a porta no `.env` ou libere a porta no sistema |
| `web` não abre / HTTP 000 | Espere o BFF e o web ficarem up; `docker compose ps` e `docker compose logs -f web-app` |
| Serviços em `Restarting` / Access denied no MySQL | Rode `docker compose down -v` e depois `docker compose up --build -d` (volume limpo; o init SQL recria schemas/usuários) |
| Login admin não funciona | Confirme que o auth está `healthy` e as variáveis `AUTH_BOOTSTRAP_ADMIN_*` no `.env`; veja `docker compose logs auth-service` |
| Aviso `GRAFANA_ADMIN_PASSWORD` | Já está no `.env.example`; se faltar no `.env`, copie de novo ou adicione `GRAFANA_ADMIN_PASSWORD=admin` |
| Build muito lento na 1ª vez | Normal (Gradle + download de imagens). Nas próximas, use só `docker compose up -d` |

---

## Fluxo rápido sugerido (após o login admin)

1. Na **home**, confira as campanhas da Uber e do iFood.  
2. Faça login como **richard@gmail.com** ou **lucas@gmail.com** (senha `password`).  
3. Credite pontos ao usuário (admin → crédito no ledger) — cada campanha custa **10** pontos por padrão.  
4. Inscreva-se na campanha e/ou abra o **baú diário**.  
5. Abra o **Kafka UI** (`:8088`) e o **Jaeger** (`:16686`) para ver o fluxo.  
6. Após a data de distribuição, consulte **Prêmios**.

Roteiro detalhado de vídeo/demo: [`docs/roteiro-video-demo.md`](docs/roteiro-video-demo.md).

---

## Documentação adicional

| Documento | Conteúdo |
| --- | --- |
| [`docs/como-subir-do-zero.md`](docs/como-subir-do-zero.md) | Guia completo de setup, portas, troubleshooting |
| [`docs/roteiro-video-demo.md`](docs/roteiro-video-demo.md) | Roteiro de demonstração (5 min) |
| [`docs/tracing-jaeger.md`](docs/tracing-jaeger.md) | Tracing distribuído (Jaeger + OpenTelemetry) |
| [`docs/especificacao-sistema-distribuido.md`](docs/especificacao-sistema-distribuido.md) | Especificação do sistema |
| [`docs/especificacao-mensagens-json.md`](docs/especificacao-mensagens-json.md) | Contratos HTTP e Kafka |
| [`docs/diagramas-de-fluxo.md`](docs/diagramas-de-fluxo.md) | Diagramas de arquitetura e sequências |
| [`integration/README.md`](integration/README.md) | Testes de integração (Go) |

---

## Resumo copy-paste

```bash
git clone <URL_DO_REPOSITORIO>
cd coupons-app
cp .env.example .env
docker compose up --build -d

# aguardar healthy…
open http://localhost:3000
# login: admin@coupons.local / admin123
```
