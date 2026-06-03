#!/usr/bin/env bash
# Publishes a test event to RabbitMQ via the Management HTTP API
# Requires RabbitMQ management enabled on localhost:15672 and guest/guest credentials

set -euo pipefail

API_URL="http://localhost:15672/api/exchanges/%2f/integracion.exchange/publish"
PAYLOAD='{"tipo":"NUEVO_FOCO","zona":"Valle del Sol","mensaje":"Incendio detectado"}'

curl -u guest:guest -H "Content-Type: application/json" -X POST "$API_URL" \
  -d "{\"properties\":{},\"routing_key\":\"integracion.routing\",\"payload\":\"$PAYLOAD\",\"payload_encoding\":\"string\"}"

echo "\nEvento publicado."
