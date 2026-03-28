#!/bin/bash
set -euo pipefail

# Roda só na primeira criação do volume (imagem oficial MySQL).
# Cria um DATABASE por serviço e um usuário com permissão apenas nesse schema.

mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" <<SQL
CREATE DATABASE IF NOT EXISTS auth
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS profile
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS campaigns
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ledger
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS prizes
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'auth_svc'@'%' IDENTIFIED WITH mysql_native_password BY '${AUTH_DB_PASSWORD}';
CREATE USER IF NOT EXISTS 'profile_svc'@'%' IDENTIFIED WITH mysql_native_password BY '${PROFILE_DB_PASSWORD}';
CREATE USER IF NOT EXISTS 'campaigns_svc'@'%' IDENTIFIED WITH mysql_native_password BY '${CAMPAIGNS_DB_PASSWORD}';
CREATE USER IF NOT EXISTS 'ledger_svc'@'%' IDENTIFIED WITH mysql_native_password BY '${LEDGER_DB_PASSWORD}';
CREATE USER IF NOT EXISTS 'prizes_svc'@'%' IDENTIFIED WITH mysql_native_password BY '${PRIZES_DB_PASSWORD}';

GRANT ALL PRIVILEGES ON auth.* TO 'auth_svc'@'%';
GRANT ALL PRIVILEGES ON profile.* TO 'profile_svc'@'%';
GRANT ALL PRIVILEGES ON campaigns.* TO 'campaigns_svc'@'%';
GRANT ALL PRIVILEGES ON ledger.* TO 'ledger_svc'@'%';
GRANT ALL PRIVILEGES ON prizes.* TO 'prizes_svc'@'%';

FLUSH PRIVILEGES;
SQL
