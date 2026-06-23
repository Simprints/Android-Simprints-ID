#!/usr/bin/env bash
# Tests for generate-version.sh
#
# Run from any directory: bash .github/scripts/test-generate-version.sh
# Exit code 0 = all tests passed, non-zero = one or more failures.

set -uo pipefail

SCRIPT="$(cd "$(dirname "$0")" && pwd)/generate-version.sh"
PASS=0
FAIL=0

# ---------------------------------------------------------------------------
# Test runner helpers
# ---------------------------------------------------------------------------

_run() {
    # _run VERSION_NAME RUN_ATTEMPT RUN_NUMBER VERSION_SUFFIX
    local env_file
    env_file="$(mktemp "${TMPDIR:-/tmp}/gen_version_test.XXXXXX")"
    _OUTPUT=$(VERSION_NAME="$1" RUN_ATTEMPT="$2" RUN_NUMBER="$3" VERSION_SUFFIX="$4" \
        GITHUB_ENV="$env_file" bash "$SCRIPT" 2>&1)
    _EXIT=$?
    _ENV_CONTENT="$(cat "$env_file" 2>/dev/null || true)"
    rm -f "$env_file"
}

_get_env() {
    # Extract a key's value written to GITHUB_ENV
    echo "$_ENV_CONTENT" | grep "^${1}=" | cut -d= -f2-
}

assert_success() {
    # assert_success LABEL EXPECTED_CODE EXPECTED_BUILD EXPECTED_FILE VERSION_NAME RUN_ATTEMPT RUN_NUMBER VERSION_SUFFIX
    local label="$1" expected_code="$2" expected_build="$3" expected_file="$4"
    shift 4
    _run "$@"

    if [ "$_EXIT" -ne 0 ]; then
        echo "FAIL [$label]: expected success but got exit $_EXIT — $_OUTPUT"
        FAIL=$(( FAIL + 1 ))
        return
    fi

    local actual_code actual_build actual_file
    actual_code="$(_get_env VERSION_CODE)"
    actual_build="$(_get_env VERSION_BUILD)"
    actual_file="$(_get_env FILE_NAME)"

    local ok=1
    if [ "$actual_code" != "$expected_code" ]; then
        echo "FAIL [$label]: VERSION_CODE expected=$expected_code actual=$actual_code"
        ok=0
    fi
    if [ "$actual_build" != "$expected_build" ]; then
        echo "FAIL [$label]: VERSION_BUILD expected=$expected_build actual=$actual_build"
        ok=0
    fi
    if [ "$actual_file" != "$expected_file" ]; then
        echo "FAIL [$label]: FILE_NAME expected=$expected_file actual=$actual_file"
        ok=0
    fi

    if [ "$ok" -eq 1 ]; then
        echo "PASS [$label]"
        PASS=$(( PASS + 1 ))
    else
        FAIL=$(( FAIL + 1 ))
    fi
}

assert_failure() {
    # assert_failure LABEL EXPECTED_ERROR_SUBSTRING VERSION_NAME RUN_ATTEMPT RUN_NUMBER VERSION_SUFFIX
    local label="$1" expected_msg="$2"
    shift 2
    _run "$@"

    if [ "$_EXIT" -eq 0 ]; then
        echo "FAIL [$label]: expected failure but script succeeded (output: $_OUTPUT)"
        FAIL=$(( FAIL + 1 ))
        return
    fi

    if ! echo "$_OUTPUT" | grep -qF "$expected_msg"; then
        echo "FAIL [$label]: expected error containing '$expected_msg', got: $_OUTPUT"
        FAIL=$(( FAIL + 1 ))
        return
    fi

    echo "PASS [$label]"
    PASS=$(( PASS + 1 ))
}

# ---------------------------------------------------------------------------
# Valid cases — formula: (year-2000)*10_000_000 + minor*100_000 + patch*1_000 + run_number
# ---------------------------------------------------------------------------

# Standard release: 2026.2.0, run 345
# = 26*10_000_000 + 2*100_000 + 0 + 345 = 260_200_345
assert_success "standard release" \
    "260200345" "345.1" "2026.2.0+dev.345" \
    "2026.2.0" 1 345 "dev"

# With non-zero patch: 2026.2.1, run 345
# = 260_000_000 + 200_000 + 1_000 + 345 = 260_201_345
assert_success "non-zero patch" \
    "260201345" "345.1" "2026.2.1+staging.345" \
    "2026.2.1" 1 345 "staging"

# Octal edge case — leading zeros in month/patch (08, 09 must not be interpreted as octal)
# 2026.08.09, run 1 → (26)*10M + 8*100K + 9*1K + 1 = 260_809_001
assert_success "octal edge case 08.09" \
    "260809001" "1.1" "2026.08.09+dev.1" \
    "2026.08.09" 1 1 "dev"

# Another octal edge: 2026.09.08, run 1 → 260_000_000 + 900_000 + 8_000 + 1 = 260_908_001
assert_success "octal edge case 09.08" \
    "260908001" "1.1" "2026.09.08+dev.1" \
    "2026.09.08" 1 1 "dev"

# Double-digit minor: 2026.21.0, run 50 → 260_000_000 + 2_100_000 + 0 + 50 = 262_100_050
assert_success "double-digit minor" \
    "262100050" "50.1" "2026.21.0+internal.50" \
    "2026.21.0" 1 50 "internal"

# Double-digit patch: 2026.2.10, run 50 → 260_000_000 + 200_000 + 10_000 + 50 = 260_210_050
assert_success "double-digit patch" \
    "260210050" "50.1" "2026.2.10+dev.50" \
    "2026.2.10" 1 50 "dev"

# Double-digit minor and patch: 2026.21.10, run 50 → 260_000_000 + 2_100_000 + 10_000 + 50 = 262_110_050
assert_success "double-digit minor and patch" \
    "262110050" "50.1" "2026.21.10+dev.50" \
    "2026.21.10" 1 50 "dev"

# Minimum valid year: 2020.1.0, run 1 → 20*10M + 100_000 + 0 + 1 = 200_100_001
assert_success "minimum year boundary" \
    "200100001" "1.1" "2020.1.0+dev.1" \
    "2020.1.0" 1 1 "dev"

# Maximum valid values: 2099.99.99, run 999 → 99*10M + 9_900_000 + 99_000 + 999 = 999_999_999
assert_success "maximum valid values" \
    "999999999" "999.1" "2099.99.99+dev.999" \
    "2099.99.99" 1 999 "dev"

# run_number 1000 — no longer wraps, adds directly
# 2026.2.0, run 1000 → 260_200_000 + 1_000 = 260_201_000
assert_success "run_number 1000 does not wrap" \
    "260201000" "1000.1" "2026.2.0+dev.1000" \
    "2026.2.0" 1 1000 "dev"

# run_number 2000 — adds directly
# 2026.2.0, run 2000 → 260_200_000 + 2_000 = 260_202_000
assert_success "run_number 2000 does not wrap" \
    "260202000" "2000.1" "2026.2.0+dev.2000" \
    "2026.2.0" 1 2000 "dev"

# Large run_number: 5001 → adds directly
# 2026.2.0, run 5001 → 260_200_000 + 5_001 = 260_205_001
assert_success "large run_number" \
    "260205001" "5001.1" "2026.2.0+dev.5001" \
    "2026.2.0" 1 5001 "dev"

# run_number at 999
assert_success "run_number at 999" \
    "260200999" "999.1" "2026.2.0+dev.999" \
    "2026.2.0" 1 999 "dev"

# run_number at 9999 — last 4-digit run number
# 2026.2.0, run 9999 → 260_200_000 + 9_999 = 260_209_999
assert_success "run_number at 9999 (last 4-digit)" \
    "260209999" "9999.1" "2026.2.0+dev.9999" \
    "2026.2.0" 1 9999 "dev"

# run_number at 10000 — first 5-digit run number
# 2026.2.0, run 10000 → 260_200_000 + 10_000 = 260_210_000
assert_success "run_number at 10000 (first 5-digit)" \
    "260210000" "10000.1" "2026.2.0+dev.10000" \
    "2026.2.0" 1 10000 "dev"

# run_number at 99999 — last 5-digit run number
# 2026.2.0, run 99999 → 260_200_000 + 99_999 = 260_299_999
assert_success "run_number at 99999 (last 5-digit)" \
    "260299999" "99999.1" "2026.2.0+dev.99999" \
    "2026.2.0" 1 99999 "dev"

# Patch at boundary 99: 2026.1.99, run 1 → 260_000_000 + 100_000 + 99_000 + 1 = 260_199_001
assert_success "patch boundary 99" \
    "260199001" "1.1" "2026.1.99+dev.1" \
    "2026.1.99" 1 1 "dev"

# Minor at boundary 99: 2026.99.0, run 1 → 260_000_000 + 9_900_000 + 0 + 1 = 269_900_001
assert_success "minor boundary 99" \
    "269900001" "1.1" "2026.99.0+dev.1" \
    "2026.99.0" 1 1 "dev"

# All components at single digit minimums: 2020.0.0, run 1 → 200_000_001
assert_success "minor and patch at 0" \
    "200000001" "1.1" "2020.0.0+dev.1" \
    "2020.0.0" 1 1 "dev"

# ---------------------------------------------------------------------------
# Invalid cases — re-run detection
# ---------------------------------------------------------------------------

assert_failure "re-run attempt=2" "re-runs are not allowed" \
    "2026.2.0" 2 345 "dev"

assert_failure "re-run attempt=3" "re-runs are not allowed" \
    "2026.2.0" 3 1 "dev"

# ---------------------------------------------------------------------------
# Invalid cases — VERSION_NAME format
# ---------------------------------------------------------------------------

assert_failure "missing patch component" "does not match required format" \
    "2026.2" 1 1 "dev"

assert_failure "2-digit year" "does not match required format" \
    "26.2.0" 1 1 "dev"

assert_failure "3-digit year" "does not match required format" \
    "202.2.0" 1 1 "dev"

assert_failure "extra 4th component" "does not match required format" \
    "2026.2.0.1" 1 1 "dev"

assert_failure "alphabetic minor" "does not match required format" \
    "2026.a.0" 1 1 "dev"

assert_failure "alphabetic patch" "does not match required format" \
    "2026.2.b" 1 1 "dev"

assert_failure "empty version name" "does not match required format" \
    "" 1 1 "dev"

assert_failure "only dots" "does not match required format" \
    "..." 1 1 "dev"

# 3-digit minor (100) is rejected by the regex before the range check
assert_failure "3-digit minor 100 rejected by format" "does not match required format" \
    "2026.100.0" 1 1 "dev"

# 3-digit patch (100) is rejected by the regex before the range check
assert_failure "3-digit patch 100 rejected by format" "does not match required format" \
    "2026.1.100" 1 1 "dev"

# ---------------------------------------------------------------------------
# Invalid cases — year out of range
# ---------------------------------------------------------------------------

assert_failure "year below minimum (2019)" "out of supported range" \
    "2019.1.0" 1 1 "dev"

assert_failure "year at 2000 (below minimum)" "out of supported range" \
    "2000.1.0" 1 1 "dev"

assert_failure "year above maximum (2100)" "out of supported range" \
    "2100.1.0" 1 1 "dev"

# ---------------------------------------------------------------------------
# Summary
# ---------------------------------------------------------------------------

TOTAL=$(( PASS + FAIL ))
echo ""
echo "Results: $PASS/$TOTAL passed"

if [ "$FAIL" -gt 0 ]; then
    exit 1
fi
