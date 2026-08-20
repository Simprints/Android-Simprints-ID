#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../../../.." && pwd)"
echo "Root: $ROOT_DIR"

FRAGMENT_OUT="$ROOT_DIR/docs/migration/metrics/results/fragments/startup_time.json"
RESULTS_DIR="$ROOT_DIR/benchmark/build/outputs/connected_android_test_additional_output"

GIT_COMMIT="$(git -C "$ROOT_DIR" rev-parse HEAD 2>/dev/null || echo "unknown")"
GIT_BRANCH="$(git -C "$ROOT_DIR" rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")"

echo "Running startup benchmark for commit=$GIT_COMMIT branch=$GIT_BRANCH"
echo "Make sure a benchmark-capable device/emulator is connected."

"$ROOT_DIR/gradlew" :benchmark:connectedBenchmarkAndroidTest

python3 "$ROOT_DIR/docs/migration/metrics/scripts/parse-startup-benchmark-results.py" \
  --input-dir "$RESULTS_DIR" \
  --output-json "$FRAGMENT_OUT"

python3 "$ROOT_DIR/docs/migration/metrics/scripts/record-metric-fragment.py" \
  "$FRAGMENT_OUT" \
  "startup_time.git_commit=$GIT_COMMIT" \
  "startup_time.git_branch=$GIT_BRANCH"

echo "Startup metrics fragment written to $FRAGMENT_OUT"
