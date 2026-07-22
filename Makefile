.PHONY: up down logs config backend-test frontend-build frontend-test smoke review-pack finalize-pack generate-masks validate-pack

up:
	docker compose up --build

down:
	docker compose down

logs:
	docker compose logs -f

config:
	docker compose config --quiet

backend-test:
	docker compose run --rm backend mvn test

frontend-build:
	docker compose run --rm frontend npm run build

frontend-test:
	docker compose run --rm frontend npm test

smoke:
	./scripts/smoke_test.sh

review-pack:
	python3 scripts/review_scene_pack.py

finalize-pack:
	python3 -u scripts/finalize_scene_pack.py

generate-masks:
	python3 scripts/generate_cell_masks.py

validate-pack:
	@test -n "$(MANIFEST)" || (echo "usage: make validate-pack MANIFEST=path/to/manifest.json"; exit 2)
	python3 scripts/validate_scene_pack.py "$(MANIFEST)"
