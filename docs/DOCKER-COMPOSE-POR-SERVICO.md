# Docker Compose por servico

Objetivo: facilitar diagnostico e subida isolada de cada microservico.

## 1) Subir infraestrutura base

```bash
docker-compose -f docker-compose.infra.yml up -d
```

Servicos de infra:
- MySQL
- Zookeeper
- Kafka
- Kafka UI

## 2) Subir servico isolado

Exemplos:

```bash
docker-compose -f docker-compose.auth.yml up -d --build
docker-compose -f docker-compose.profile.yml up -d --build
docker-compose -f docker-compose.ledger.yml up -d --build
docker-compose -f docker-compose.campaigns.yml up -d --build
docker-compose -f docker-compose.prizes.yml up -d --build
docker-compose -f docker-compose.bff.yml up -d --build
```

## 3) Ver status rapido

```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

## 4) Logs de um servico

Exemplo:

```bash
docker logs -f coupons-campaigns-service
```

## 5) Derrubar apenas um servico

Exemplo:

```bash
docker-compose -f docker-compose.campaigns.yml down
```

## 6) Derrubar infraestrutura

```bash
docker-compose -f docker-compose.infra.yml down
```
