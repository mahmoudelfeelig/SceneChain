#!/usr/bin/env sh
set -eu

: "${APP_DATABASE_PASSWORD:?APP_DATABASE_PASSWORD is required}"

psql --set=ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
  --set=app_password="$APP_DATABASE_PASSWORD" <<'SQL'
SELECT format('CREATE ROLE scenechain_app LOGIN PASSWORD %L', :'app_password')
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'scenechain_app') \gexec

GRANT CONNECT ON DATABASE scenechain TO scenechain_app;
GRANT USAGE ON SCHEMA public TO scenechain_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO scenechain_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO scenechain_app;
ALTER DEFAULT PRIVILEGES FOR ROLE scenechain_migration IN SCHEMA public
  GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO scenechain_app;
ALTER DEFAULT PRIVILEGES FOR ROLE scenechain_migration IN SCHEMA public
  GRANT USAGE, SELECT ON SEQUENCES TO scenechain_app;
SQL
