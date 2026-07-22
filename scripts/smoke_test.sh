#!/usr/bin/env sh
set -eu

base_url="${SCENECHAIN_URL:-http://localhost:8088}"
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT

curl -fsS "$base_url/" >/dev/null
curl -fsS "$base_url/api/pack/status" >"$work/pack.json"

cross_status="$(curl -sS -o /dev/null -w '%{http_code}' -H 'Origin: https://attacker.example' \
  -H 'Sec-Fetch-Site: cross-site' -H 'Content-Type: application/json' \
  -d '{"handle":"SC-ABCD-2345","mode":"direct"}' "$base_url/api/auth/attempts")"
test "$cross_status" = "403"

curl -fsS -c "$work/cookies" -H 'Content-Type: application/json' \
  -d '{"informed":true,"adult":true,"voluntary":true,"researchMetrics":true,"deletionRights":true,"comprehensionPassed":true}' \
  "$base_url/api/enrollments/start" >"$work/enrollment.json"

python3 - "$work/pack.json" "$work/enrollment.json" "$work/stages.json" "$work/complete.json" <<'PY'
import json, sys
pack=json.load(open(sys.argv[1])); enrollment=json.load(open(sys.argv[2]))
assert pack["mode"] == "formal" and pack["sceneCount"] == 48 and pack["recruitmentEnabled"] is True
assert len(enrollment["scenes"]) == 5 and len({s["family"] for s in enrollment["scenes"]}) == 5
assert all(s["eligibleCells"] == list(range(384)) for s in enrollment["scenes"])
stages=[{"sceneId":s["id"],"cellId":0,"actionId":0} for s in enrollment["scenes"]]
json.dump({"stages":stages},open(sys.argv[3],"w"))
json.dump({"stages":stages,"password":"smoke-test-only-password-2026","totalMs":1000,"stageMs":[200]*5},open(sys.argv[4],"w"))
PY

csrf="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["csrfToken"])' "$work/enrollment.json")"
for confirmation in 1 2; do
  curl -fsS -b "$work/cookies" -c "$work/cookies" -H 'Content-Type: application/json' -H "X-CSRF-Token: $csrf" \
    --data-binary "@$work/stages.json" "$base_url/api/enrollments/confirmation" >"$work/confirmation-$confirmation.json"
done
curl -fsS -b "$work/cookies" -c "$work/cookies" -H 'Content-Type: application/json' -H "X-CSRF-Token: $csrf" \
  --data-binary "@$work/complete.json" "$base_url/api/enrollments/complete" >"$work/account.json"

handle="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["handle"])' "$work/account.json")"
curl -fsS -b "$work/cookies" -c "$work/cookies" -H 'Content-Type: application/json' \
  -d '{"viewportWidth":1440,"viewportHeight":900,"inputMethod":"mouse","browserFamily":"chromium"}' \
  "$base_url/api/study/start" >"$work/state.json"
condition="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["condition"])' "$work/state.json")"

if test "$condition" = "password"; then
  curl -fsS -b "$work/cookies" -c "$work/auth-cookies" -H 'Content-Type: application/json' \
    -d "{\"handle\":\"$handle\"}" "$base_url/api/auth/password/attempts" >"$work/attempt.json"
  auth_csrf="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["csrfToken"])' "$work/attempt.json")"
  curl -fsS -b "$work/auth-cookies" -c "$work/session-cookies" -H 'Content-Type: application/json' \
    -H "X-CSRF-Token: $auth_csrf" -d '{"password":"smoke-test-only-password-2026"}' \
    "$base_url/api/auth/password" >"$work/result.json"
else
  curl -fsS -b "$work/cookies" -c "$work/auth-cookies" -H 'Content-Type: application/json' \
    -d "{\"handle\":\"$handle\",\"mode\":\"$condition\"}" "$base_url/api/auth/attempts" >"$work/attempt.json"
  python3 - "$work/enrollment.json" "$work/attempt.json" "$work/login.json" "$condition" <<'PY'
import json, sys
enrollment=json.load(open(sys.argv[1])); attempt=json.load(open(sys.argv[2])); condition=sys.argv[4]
by_id={scene["id"]:scene for scene in attempt["scenes"]}
stages=[]
for scene in enrollment["scenes"]:
    stage={"sceneId":scene["id"],"actionId":0}
    if condition == "direct": stage["cellId"]=0
    else: stage["markerId"]=by_id[scene["id"]]["overlay"][0]
    stages.append(stage)
json.dump({"stages":stages,"totalMs":0,"stageMs":[0]*5},open(sys.argv[3],"w"))
PY
  auth_csrf="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["csrfToken"])' "$work/attempt.json")"
  curl -fsS -b "$work/auth-cookies" -c "$work/session-cookies" -H 'Content-Type: application/json' \
    -H "X-CSRF-Token: $auth_csrf" --data-binary "@$work/login.json" \
    "$base_url/api/auth/attempts/complete" >"$work/result.json"
fi

python3 -c 'import json,sys; assert json.load(open(sys.argv[1]))["authenticated"] is True' "$work/result.json"
curl -fsS -b "$work/session-cookies" "$base_url/api/me" >/dev/null
curl -fsS -b "$work/session-cookies" "$base_url/api/study/state" >"$work/state-after.json"
python3 -c 'import json,sys; s=json.load(open(sys.argv[1])); assert s["phase"]=="practice" and s["practiceSuccesses"]==1' "$work/state-after.json"

echo "SceneChain formal-pack and assigned-study integration smoke test passed"
