#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../../../.." && pwd)"
echo "Root: $ROOT_DIR"

SCENARIO_FILE="$ROOT_DIR/docs/migration/metrics/scripts/gradle-profiler.scenarios"

# Intermediate raw profiler output goes in build/ (ephemeral, not committed).
INTERMEDIATE_DIR="${1:-$ROOT_DIR/docs/build/migration-metrics/gradle-profiler}"
echo "Intermediate Dir: $INTERMEDIATE_DIR"

# Final parsed fragment is persisted in docs/migration/metrics/results/.
FRAGMENT_OUT="$ROOT_DIR/docs/migration/metrics/results/fragments/build_time.json"

if ! command -v gradle-profiler >/dev/null 2>&1; then
  echo "ERROR: gradle-profiler is not installed or not on PATH" >&2
  echo "Install with: brew install gradle-profiler" >&2
  exit 1
fi

GIT_COMMIT="$(git -C "$ROOT_DIR" rev-parse HEAD 2>/dev/null || echo "unknown")"
GIT_BRANCH="$(git -C "$ROOT_DIR" rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")"

echo "Recording build-time metrics for commit=$GIT_COMMIT branch=$GIT_BRANCH"

# gradle-profiler requires the output directory to not exist — it creates it
# itself. Remove any leftover from a previous run before proceeding.
rm -rf "$INTERMEDIATE_DIR"
mkdir -p "$INTERMEDIATE_DIR"

gradle-profiler \
  --benchmark \
  --csv-format long \
  --project-dir "$ROOT_DIR" \
  --scenario-file "$SCENARIO_FILE" \
  --output-dir "$INTERMEDIATE_DIR"

CSV_PATH="$INTERMEDIATE_DIR/benchmark.csv"
echo "CSV_PATH Dir: $CSV_PATH"

#
#if [ ! -f "$CSV_PATH" ]; then
#  # gradle-profiler sometimes creates a versioned subdirectory when the target
#  # already exists; fall back to finding the CSV anywhere under INTERMEDIATE_DIR.
#  CSV_PATH="$(find "$INTERMEDIATE_DIR" -name "benchmark.csv" -maxdepth 2 | head -n 1)"
#fi
#if [ -z "$CSV_PATH" ] || [ ! -f "$CSV_PATH" ]; then
#  echo "ERROR: benchmark.csv not found under $INTERMEDIATE_DIR" >&2
#  exit 1
#fi

# Parse CSV into metric keys and write initial fragment.
python3 "$ROOT_DIR/docs/migration/metrics/scripts/parse-gradle-profiler-csv.py" \
  "$CSV_PATH" \
  "$FRAGMENT_OUT"

# Stamp the fragment with the git coordinates so it can be correlated
# with snapshots from collect-compose-migration-metrics.py.
python3 "$ROOT_DIR/docs/migration/metrics/scripts/record-metric-fragment.py" \
  "$FRAGMENT_OUT" \
  "build_time.git_commit=$GIT_COMMIT" \
  "build_time.git_branch=$GIT_BRANCH"

echo "Build-time fragment written to $FRAGMENT_OUT"
echo "  git.commit : $GIT_COMMIT"
echo "  git.branch : $GIT_BRANCH"
echo "Intermediate profiler output kept at $INTERMEDIATE_DIR"
