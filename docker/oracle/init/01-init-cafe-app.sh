#!/bin/bash
set -euo pipefail

if [ -z "${APP_USER:-}" ] || [ -z "${APP_USER_PASSWORD:-}" ]; then
  echo "APP_USER and APP_USER_PASSWORD must be set."
  exit 1
fi

echo "Initializing Coffee Chain schema and seed data for ${APP_USER}..."

sqlplus -L "${APP_USER}/${APP_USER_PASSWORD}@//localhost:1521/XEPDB1" <<'SQL'
WHENEVER SQLERROR EXIT SQL.SQLCODE
SET DEFINE OFF;
SET SQLBLANKLINES ON;
@/container-entrypoint-initdb.d/schema.run
@/container-entrypoint-initdb.d/seed.run
EXIT;
SQL

echo "Coffee Chain database initialization completed."
