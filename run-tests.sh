#!/usr/bin/env bash
# Runs all backend and frontend tests non-interactively. Exits non-zero if either suite fails.
set -u
cd "$(dirname "$0")"

status=0

echo "==> Backend tests"
(cd backend && ./gradlew test --console=plain) || status=1

echo "==> Frontend tests"
(cd frontend && { [ -d node_modules ] || npm ci; } && CI=true npm test) || status=1

if [ "$status" -eq 0 ]; then
	echo "==> All tests passed"
else
	echo "==> Tests failed" >&2
fi
exit "$status"
