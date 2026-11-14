#!/usr/bin/env bash
# Measures clean and incremental build times with gradle-profiler and writes
# results/fragments/build_time.json stamped with the current commit.
set -euo pipefail
source "$(dirname "$0")/common.sh"

require_command gradle-profiler "Install with: brew install gradle-profiler"
require_clean_tree

INTERMEDIATE_DIR="$INTERMEDIATE_ROOT/gradle-profiler"
FRAGMENT_OUT="$FRAGMENT_DIR/build_time.json"

echo "Recording build-time metrics for commit=$GIT_COMMIT branch=$GIT_BRANCH"
rm -rf "$INTERMEDIATE_DIR"
mkdir -p "$INTERMEDIATE_DIR"

gradle-profiler \
  --benchmark \
  --csv-format long \
  --project-dir "$ROOT_DIR" \
  --scenario-file "$SCRIPTS_DIR/gradle-profiler.scenarios" \
  --output-dir "$INTERMEDIATE_DIR"

python3 "$SCRIPTS_DIR/parse-gradle-profiler-csv.py" "$INTERMEDIATE_DIR/benchmark.csv" "$FRAGMENT_OUT"

python3 "$SCRIPTS_DIR/record-metric-fragment.py" "$FRAGMENT_OUT" \
  "build_time.git_commit=$GIT_COMMIT" \
  "build_time.git_branch=$GIT_BRANCH" \
  "build_time.machine=$(machine_description)" \
  "build_time.variant=debug"

echo "Build-time fragment written to $FRAGMENT_OUT (raw profiler output in $INTERMEDIATE_DIR)"
