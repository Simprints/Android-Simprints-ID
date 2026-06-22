#!/usr/bin/env bash
# Validates VERSION_NAME and computes VERSION_CODE and FILE_NAME.
#
# Required environment variables:
#   VERSION_NAME    e.g. "2026.2.0"
#   VERSION_SUFFIX  e.g. "dev", "staging", "internal"
#   RUN_ATTEMPT     github.run_attempt — must be 1 (re-runs are not allowed)
#   RUN_NUMBER      github.run_number
#
# Outputs are appended to $GITHUB_ENV (or stdout if GITHUB_ENV is unset).

set -euo pipefail

# Re-runs are disabled because VERSION_CODE uses only run_number for its sequence slot.
if [ "${RUN_ATTEMPT}" -ne 1 ]; then
    echo "ERROR: Workflow re-runs are not allowed for this build; trigger a new run instead" >&2
    exit 1
fi

# Validate VERSION_NAME format: YYYY.M.P (e.g. 2026.2.0)
if ! echo "$VERSION_NAME" | grep -qE '^[0-9]{4}\.[0-9]{1,2}\.[0-9]{1,2}$'; then
    echo "ERROR: VERSION_NAME '$VERSION_NAME' does not match required format YYYY.M.P" >&2
    exit 1
fi

IFS='.' read -r VERSION_YEAR VERSION_MINOR VERSION_PATCH <<< "$VERSION_NAME"
# Force base-10 to avoid octal misinterpretation (e.g. 08, 09)
VERSION_YEAR=$((10#$VERSION_YEAR))
VERSION_MINOR=$((10#$VERSION_MINOR))
VERSION_PATCH=$((10#$VERSION_PATCH))

if [ "$VERSION_YEAR" -lt 2020 ] || [ "$VERSION_YEAR" -gt 2099 ]; then
    echo "ERROR: Version year $VERSION_YEAR is out of supported range (2020-2099)" >&2
    exit 1
fi

if [ "$VERSION_MINOR" -gt 99 ] || [ "$VERSION_PATCH" -gt 99 ]; then
    echo "ERROR: Minor ($VERSION_MINOR) and patch ($VERSION_PATCH) must each be 0-99" >&2
    exit 1
fi

VERSION_CODE_CALCULATED=$(( (VERSION_YEAR - 2000) * 10000000 + VERSION_MINOR * 100000 + VERSION_PATCH * 1000 + RUN_NUMBER ))

if [ "$VERSION_CODE_CALCULATED" -gt 2100000000 ]; then
    echo "ERROR: VERSION_CODE $VERSION_CODE_CALCULATED exceeds Android max 2100000000" >&2
    exit 1
fi

DEST="${GITHUB_ENV:-/dev/stdout}"
echo "VERSION_CODE=$VERSION_CODE_CALCULATED" >> "$DEST"
echo "VERSION_BUILD=${RUN_NUMBER}.${RUN_ATTEMPT}" >> "$DEST"
echo "FILE_NAME=${VERSION_NAME}+${VERSION_SUFFIX}.${RUN_NUMBER}" >> "$DEST"
