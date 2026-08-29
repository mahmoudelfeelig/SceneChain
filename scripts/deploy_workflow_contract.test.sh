#!/bin/sh
set -eu

WORKFLOW=.github/workflows/deploy-hetzner.yml
EXPECTED_GATEWAY='mahmoudelfeelig/HetznerReleaseGateway/.github/workflows/release.yml'

grep -Fq 'workflow_run:' "$WORKFLOW"
grep -Fq 'workflows: ["SceneChain CI"]' "$WORKFLOW"
grep -Fq "github.event.workflow_run.conclusion == 'success'" "$WORKFLOW"
grep -Fq "github.event.workflow_run.event == 'push'" "$WORKFLOW"
grep -Fq "github.event.workflow_run.head_branch == 'main'" "$WORKFLOW"
grep -Fq 'github.event.workflow_run.head_repository.full_name == github.repository' "$WORKFLOW"

grep -Fq 'actions: read' "$WORKFLOW"
grep -Fq 'contents: read' "$WORKFLOW"
grep -Fq 'id-token: write' "$WORKFLOW"
test "$(grep -Ec '^[[:space:]]*uses: .+@[0-9a-f]{40}$' "$WORKFLOW")" -eq 1
grep -Eq "uses: ${EXPECTED_GATEWAY}@[0-9a-f]{40}$" "$WORKFLOW"
grep -Fq 'app: scenechain' "$WORKFLOW"
grep -Fq 'source_sha: ${{ github.event.workflow_run.head_sha }}' "$WORKFLOW"
grep -Fq 'ci_run_id: ${{ github.event.workflow_run.id }}' "$WORKFLOW"

if grep -Eq '^[[:space:]]+(run|steps):|^[[:space:]]*secrets:|secrets\.' "$WORKFLOW"; then
  echo "The production caller must not execute host commands or inherit credentials." >&2
  exit 1
fi

for retired in \
  deploy/Caddyfile.example \
  deploy/Caddyfile.hetzner.example \
  deploy/docker-compose.hetzner.yml \
  scripts/deploy_hetzner.sh
do
  grep -Fq 'Retired:' "$retired" || grep -Fq 'is retired' "$retired"
  if grep -Eiq '/opt/|reverse_proxy|external:[[:space:]]*true|docker compose|ssh|scp|rsync' "$retired"; then
    echo "Retired deployment material still exposes host behavior: $retired" >&2
    exit 1
  fi
done

echo "Central deployment caller contract passed."
