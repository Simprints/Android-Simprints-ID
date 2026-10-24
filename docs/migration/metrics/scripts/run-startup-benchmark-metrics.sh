#!/usr/bin/env bash
# Runs the cold-startup macrobenchmark on the connected device (minified `benchmark` variant)
# and writes results/fragments/startup_time.json stamped with the current commit.
#
# Extra arguments are passed to Gradle, e.g. -Psimprints.compose.feature-dashboard=true
# for a `p2-flag:<module>` snapshot (requires the library benchmark build type).
set -euo pipefail
source "$(dirname "$0")/common.sh"

require_command adb "Install the Android SDK platform-tools."
require_clean_tree
DEVICE="$(device_description)"

FRAGMENT_OUT="$FRAGMENT_DIR/startup_time.json"
RESULTS_DIR="$ROOT_DIR/benchmark/build/outputs/connected_android_test_additional_output"
GRADLE_LOG="$INTERMEDIATE_ROOT/startup/gradle.log"

echo "Running startup benchmark for commit=$GIT_COMMIT on $DEVICE"
rm -rf "$RESULTS_DIR"
mkdir -p "$(dirname "$GRADLE_LOG")"
"$ROOT_DIR/gradlew" -p "$ROOT_DIR" :benchmark:connectedBenchmarkAndroidTest "$@" 2>&1 | tee "$GRADLE_LOG"

# The connected test task reports BUILD SUCCESSFUL (and exit code 0) even when the APKs could not be
# installed or the runner crashed, so check for the benchmark output before parsing.
if ! find "$RESULTS_DIR" -name '*benchmarkData*.json' -print -quit 2>/dev/null | grep -q .; then
  echo "ERROR: the startup benchmark produced no results on $DEVICE." >&2
  if grep -qE 'AndroidTestRunner failed|Failure \[' "$GRADLE_LOG"; then
    echo "The test runner reported:" >&2
    grep -E 'AndroidTestRunner failed|Failure \[' "$GRADLE_LOG" | sort -u >&2
  fi
  echo "Common causes: an app signed with another key is installed (uninstall com.simprints.id), the device is out of" >&2
  echo "storage (INSTALL_FAILED_INSUFFICIENT_STORAGE), or the device is locked. Full Gradle output: $GRADLE_LOG" >&2
  exit 1
fi

python3 "$SCRIPTS_DIR/parse-startup-benchmark-results.py" \
  --input-dir "$RESULTS_DIR" \
  --output-json "$FRAGMENT_OUT"

python3 "$SCRIPTS_DIR/record-metric-fragment.py" "$FRAGMENT_OUT" \
  "startup_time.git_commit=$GIT_COMMIT" \
  "startup_time.git_branch=$GIT_BRANCH" \
  "startup_time.device=$DEVICE" \
  "startup_time.variant=benchmark" \
  "startup_time.gradle_args=$*"

echo "Startup metrics fragment written to $FRAGMENT_OUT"
