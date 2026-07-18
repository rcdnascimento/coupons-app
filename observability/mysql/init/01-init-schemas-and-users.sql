-- Roda só na primeira criação do volume (imagem oficial MySQL).
-- Cria um DATABASE por serviço e um usuário com permissão apenas nesse schema.
-- Senhas alinhadas ao .env.example (altere aqui se mudar as senhas do .env antes do primeiro up).

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
CREATE DATABASE IF NOT EXISTS daily_chest
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'auth_svc'@'%' IDENTIFIED WITH mysql_native_password BY 'auth_dev_change_me';
CREATE USER IF NOT EXISTS 'profile_svc'@'%' IDENTIFIED WITH mysql_native_password BY 'profile_dev_change_me';
CREATE USER IF NOT EXISTS 'campaigns_svc'@'%' IDENTIFIED WITH mysql_native_password BY 'campaigns_dev_change_me';
CREATE USER IF NOT EXISTS 'ledger_svc'@'%' IDENTIFIED WITH mysql_native_password BY 'ledger_dev_change_me';
CREATE USER IF NOT EXISTS 'prizes_svc'@'%' IDENTIFIED WITH mysql_native_password BY 'prizes_dev_change_me';
CREATE USER IF NOT EXISTS 'daily_chest_svc'@'%' IDENTIFIED WITH mysql_native_password BY 'daily_chest_dev_change_me';

GRANT ALL PRIVILEGES ON auth.* TO 'auth_svc'@'%';
GRANT ALL PRIVILEGES ON profile.* TO 'profile_svc'@'%';
GRANT ALL PRIVILEGES ON campaigns.* TO 'campaigns_svc'@'%';
GRANT ALL PRIVILEGES ON ledger.* TO 'ledger_svc'@'%';
GRANT ALL PRIVILEGES ON prizes.* TO 'prizes_svc'@'%';
GRANT ALL PRIVILEGES ON daily_chest.* TO 'daily_chest_svc'@'%';

FLUSH PRIVILEGES;
