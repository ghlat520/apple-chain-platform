#!/usr/bin/env bash
# OpenLineage smoke test for DataHub v0.13.3 GMS
#
# Verifies the CORRECT endpoint path discovered from the swagger api-docs:
#   POST ${GMS}/openapi/openlineage/api/v1/lineage
#
# (An earlier wrong path `/openapi/v2/lineage` returns 404 — do not use it.)
#
# Usage:
#   bash openlineage-smoke-test.sh              # defaults to localhost:18081
#   GMS_URL=http://remote:18081 bash openlineage-smoke-test.sh

set -u
GMS_URL="${GMS_URL:-http://localhost:18081}"
ENDPOINT="$GMS_URL/openapi/openlineage/api/v1/lineage"

echo "=== 1/4: GMS health ==="
HEALTH=$(curl -sf -m 5 "$GMS_URL/health" && echo OK || echo FAIL)
echo "health: $HEALTH"

echo ""
echo "=== 2/4: GMS /config (version) ==="
curl -sf -m 5 "$GMS_URL/config" | head -8

echo ""
echo "=== 3/4: Build OpenLineage RunEvent payload ==="
NOW=$(date -u +"%Y-%m-%dT%H:%M:%S.000Z")
RUN_ID=$(uuidgen | tr 'A-Z' 'a-z')
PAYLOAD=$(cat <<EOF
{
  "eventType": "COMPLETE",
  "eventTime": "$NOW",
  "producer": "https://github.com/apple-chain/bigdata",
  "schemaURL": "https://openlineage.io/spec/2-0-2/OpenLineage.json#/definitions/RunEvent",
  "run": {
    "runId": "$RUN_ID",
    "facets": {}
  },
  "job": {
    "namespace": "apple_chain.bigdata",
    "name": "smoke_test_job",
    "facets": {}
  },
  "inputs": [],
  "outputs": [
    {
      "namespace": "clickhouse://apple-clickhouse:9000",
      "name": "apple_bigdata_ods.smoke_test_table",
      "facets": {}
    }
  ]
}
EOF
)
echo "$PAYLOAD" | head -20

echo ""
echo "=== 4/4: POST $ENDPOINT ==="
HTTP_CODE=$(curl -s -o /tmp/ol-resp.txt -w "%{http_code}" \
  -X POST "$ENDPOINT" \
  -H "Content-Type: application/json" \
  -d "$PAYLOAD")
echo "HTTP $HTTP_CODE"
echo "Body:"
cat /tmp/ol-resp.txt
echo ""

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "201" ] || [ "$HTTP_CODE" = "204" ]; then
  echo ""
  echo "SUCCESS — DataHub accepted the lineage event."
  exit 0
else
  echo ""
  echo "WARN — non-2xx. Likely schema init not yet complete. See RESTORE.md § 'DataHub 500'."
  exit 1
fi
