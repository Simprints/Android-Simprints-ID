#!/usr/bin/env bash
# Shared helpers for the metrics run-*.sh scripts. Source it; do not execute it.

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
SCRIPTS_DIR="$ROOT_DIR/docs/migration/metrics/scripts"
FRAGMENT_DIR="$ROOT_DIR/docs/migration/metrics/results/fragments"
INTERMEDIATE_ROOT="$ROOT_DIR/docs/build/migration-metrics"

GIT_COMMIT="$(git -C "$ROOT_DIR" rev-parse HEAD)"
GIT_BRANCH="$(git -C "$ROOT_DIR" rev-parse --abbrev-ref HEAD)"

# Measurements are stamped with HEAD, so they must be taken on exactly that code.
require_clean_tree() {
  local dirty
  dirty="$(git -C "$ROOT_DIR" status --porcelain | grep -v ' docs/migration/metrics/results/' || true)"
  if [[ -n "$dirty" && "${METRICS_ALLOW_DIRTY:-0}" != "1" ]]; then
    echo "ERROR: working tree has uncommitted changes; commit or stash them (or set METRICS_ALLOW_DIRTY=1 for ad hoc runs):" >&2
    echo "$dirty" >&2
    exit 1
  fi
}

machine_description() {
  local cpu
  if [[ "$(uname -s)" == "Darwin" ]]; then
    cpu="$(sysctl -n machdep.cpu.brand_string 2>/dev/null || uname -m)"
  else
    cpu="$(grep -m1 'model name' /proc/cpuinfo 2>/dev/null | cut -d: -f2 | xargs || uname -m)"
  fi
  echo "$(uname -s) $(uname -m) / $cpu"
}

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "ERROR: '$1' is not installed or not on PATH. $2" >&2
    exit 1
  fi
}

# Exactly one device must be connected for on-device measurements.
device_description() {
  local count
  count="$(adb devices | awk 'NR>1 && $2=="device"' | wc -l | tr -d ' ')"
  if [[ "$count" != "1" ]]; then
    echo "ERROR: expected exactly one connected device, found $count" >&2
    exit 1
  fi
  echo "$(adb shell getprop ro.product.manufacturer | tr -d '\r') $(adb shell getprop ro.product.model | tr -d '\r') / API $(adb shell getprop ro.build.version.sdk | tr -d '\r')"
}
