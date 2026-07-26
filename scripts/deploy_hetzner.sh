#!/usr/bin/env sh
set -eu

cd /opt/scenechain

test -f deploy/.env
docker network inspect web >/dev/null

compose() {
  docker compose \
    --env-file deploy/.env \
    -f docker-compose.yml \
    -f docker-compose.prod.yml \
    -f deploy/docker-compose.hetzner.yml \
    "$@"
}

compose config --quiet
compose up -d --build --remove-orphans

attempt=1
while [ "$attempt" -le 60 ]; do
  if compose exec -T frontend wget -qO- http://127.0.0.1/api/pack/status >/dev/null; then
    compose ps
    exit 0
  fi
  attempt=$((attempt + 1))
  sleep 2
done

compose logs --no-color --tail=200 postgres redis backend frontend
exit 1
