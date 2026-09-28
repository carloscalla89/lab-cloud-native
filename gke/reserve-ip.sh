#!/usr/bin/env bash
# Reserva una IP estatica global para el Gateway de entrada. Idempotente.
set -euo pipefail
source "$(dirname "$0")/config.env"

if gcloud compute addresses describe "$GATEWAY_IP_NAME" \
     --global --project "$PROJECT_ID" >/dev/null 2>&1; then
  echo "La IP '$GATEWAY_IP_NAME' ya existe."
else
  gcloud compute addresses create "$GATEWAY_IP_NAME" \
    --global --project "$PROJECT_ID"
fi

IP="$(gcloud compute addresses describe "$GATEWAY_IP_NAME" \
  --global --project "$PROJECT_ID" --format='value(address)')"
echo "IP estatica '$GATEWAY_IP_NAME': $IP"
echo "Para fijarla en el Gateway, agrega en csm/gateway.yaml bajo spec:"
echo "  addresses:"
echo "    - type: NamedAddress"
echo "      value: $GATEWAY_IP_NAME"
