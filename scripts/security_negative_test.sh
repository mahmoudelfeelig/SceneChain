#!/usr/bin/env sh
set -eu
base_url="${SCENECHAIN_URL:-http://localhost:8088}"
work="$(mktemp -d)"; trap 'rm -rf "$work"' EXIT

status() { curl -sS -o /dev/null -w '%{http_code}' "$@"; }

test "$(status -H 'Origin: https://evil.example' -H 'Sec-Fetch-Site: cross-site' -H 'Content-Type: application/json' \
  -d '{"handle":"SC-ABCD-2345","mode":"direct"}' "$base_url/api/auth/attempts")" = "403"
test "$(status -H 'Content-Type: application/json' -d '{"handle":"SC-ABCD-2345","mode":"invalid"}' "$base_url/api/auth/attempts")" = "400"
test "$(status -H 'Content-Type: application/json' -d '{"handle":"not-an-account","mode":"direct"}' "$base_url/api/auth/attempts")" = "400"
consent_status="$(status -H 'Content-Type: application/json' -d '{"adult":false,"voluntary":true,"researchMetrics":true,"deletionRights":true}' "$base_url/api/enrollments/start")"
case "$consent_status" in 400|503) ;; *) exit 1 ;; esac
test "$(status -H 'Content-Type: application/json' -d '{"stages":[],"totalMs":0,"stageMs":[]}' "$base_url/api/auth/attempts/complete")" = "400"
test "$(status "$base_url/api/admin/research/events.csv")" = "404"
test "$(status "$base_url/api/admin/research/outcomes.csv")" = "404"
test "$(status -H 'Content-Type: application/json' -d '{"handle":"SC-ABCD-2345","condition":"shielded","completeChainSuccess":true,"observationCount":1,"attemptCount":1,"recordingUsed":false}' "$base_url/api/admin/research/observer")" = "404"

curl -fsS -c "$work/cookies" -H 'Content-Type: application/json' \
  -d '{"handle":"SC-ZZZZ-ZZZZ","mode":"direct"}' "$base_url/api/auth/attempts" >"$work/attempt.json"
csrf="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["csrfToken"])' "$work/attempt.json")"
python3 - "$work/attempt.json" "$work/bad.json" <<'PY'
import json,sys
a=json.load(open(sys.argv[1])); ids=[s["id"] for s in a["scenes"][:5]]
json.dump({"stages":[{"sceneId":i,"cellId":383,"actionId":3} for i in ids],"totalMs":0,"stageMs":[0]*5},open(sys.argv[2],"w"))
PY
test "$(status -b "$work/cookies" -H 'Content-Type: application/json' -H "X-CSRF-Token: $csrf" --data-binary "@$work/bad.json" "$base_url/api/auth/attempts/complete")" = "401"
test "$(status -b "$work/cookies" -H 'Content-Type: application/json' -H "X-CSRF-Token: $csrf" --data-binary "@$work/bad.json" "$base_url/api/auth/attempts/complete")" = "401"

echo "SceneChain security negative tests passed"
