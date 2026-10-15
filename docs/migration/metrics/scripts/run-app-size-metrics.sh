#!/usr/bin/env bash
# Builds the minified `benchmark` app bundle and measures its size: raw AAB bytes and the
# download size computed by bundletool for a fixed reference device. Writes
# results/fragments/app_size.json stamped with the current commit.
#
# Extra arguments are passed to Gradle (same convention as run-startup-benchmark-metrics.sh).
set -euo pipefail
source "$(dirname "$0")/common.sh"

require_command bundletool "Install with: brew install bundletool"
require_clean_tree

INTERMEDIATE_DIR="$INTERMEDIATE_ROOT/app-size"
FRAGMENT_OUT="$FRAGMENT_DIR/app_size.json"
AAB="$ROOT_DIR/id/build/outputs/bundle/benchmark/id-benchmark.aab"
APKS="$INTERMEDIATE_DIR/id-benchmark.apks"
DEVICE_SPEC="$SCRIPTS_DIR/reference-device-spec.json"

echo "Recording app size metrics for commit=$GIT_COMMIT branch=$GIT_BRANCH"
rm -rf "$INTERMEDIATE_DIR"
mkdir -p "$INTERMEDIATE_DIR"

"$ROOT_DIR/gradlew" -p "$ROOT_DIR" :id:bundleBenchmark "$@"

# Without --ks, bundletool signs with ~/.android/debug.keystore.
bundletool build-apks --bundle="$AAB" --output="$APKS" --overwrite

# get-size prints a CSV header ("MIN,MAX") followed by one line of values.
REFERENCE_SIZE="$(bundletool get-size total --apks="$APKS" --device-spec="$DEVICE_SPEC" | tail -n 1)"
RANGE_SIZE="$(bundletool get-size total --apks="$APKS" | tail -n 1)"

python3 "$SCRIPTS_DIR/record-metric-fragment.py" --replace "$FRAGMENT_OUT" \
  "app_size.aab_bytes=$(wc -c < "$AAB" | tr -d ' ')" \
  "app_size.download_bytes_reference_device=${REFERENCE_SIZE%%,*}" \
  "app_size.download_bytes_min=${RANGE_SIZE%%,*}" \
  "app_size.download_bytes_max=${RANGE_SIZE##*,}" \
  "app_size.git_commit=$GIT_COMMIT" \
  "app_size.git_branch=$GIT_BRANCH" \
  "app_size.variant=benchmark" \
  "app_size.gradle_args=$*"

echo "App size fragment written to $FRAGMENT_OUT"
