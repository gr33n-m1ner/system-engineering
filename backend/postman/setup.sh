#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

docker exec -i massage-parlor-db psql -U user -d massage_parlor_db < "$SCRIPT_DIR/cleanup.sql"
docker exec -i massage-parlor-db psql -U user -d massage_parlor_db < "$SCRIPT_DIR/create-admin.sql"
