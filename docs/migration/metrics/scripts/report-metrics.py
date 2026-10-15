#!/usr/bin/env python3
"""Compare a metrics snapshot with the baseline and the previous milestone snapshot.

Prints a Markdown report and exits with status 1 when a tracked metric regresses beyond its
threshold, so the metrics agent and humans apply the same rule.
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any

# metric key -> (allowed relative increase vs. baseline/previous, human label)
THRESHOLDS: dict[str, tuple[float, str]] = {
    "startup_time.timetoinitialdisplayms_median": (0.10, "Cold startup TTID median (ms)"),
    "startup_time.timetofulldisplayms_median": (0.10, "Cold startup TTFD median (ms)"),
    "build_time.clean_build_seconds_median": (0.15, "Clean build median (s)"),
    "build_time.incremental_core_abi_change_seconds_median": (0.15, "Incremental core ABI change median (s)"),
    "build_time.incremental_feature_non_abi_change_seconds_median": (0.15, "Incremental feature non-ABI change median (s)"),
    "app_size.download_bytes_reference_device": (0.05, "Download size, reference device (bytes)"),
    "coverage.excluded_annotation_count": (0.0, "Coverage exclusions (count)"),
}
INFORMATIONAL = {
    "module.phase1_done_pct": "Phase 1 modules done (%)",
    "module.conversion_pct": "Module conversion (%)",
    "interop.total_nodes": "Interop nodes",
    "coverage.line_pct": "Line coverage (%)",
}
ENVIRONMENT_KEYS = ("startup_time.device", "build_time.machine", "build_time.tool")
MILESTONE_PREFIXES = ("baseline", "pre-compose", "p1:", "p2-flag:", "p2:")


def load_history(path: Path) -> list[dict[str, Any]]:
    if not path.exists():
        return []
    return [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]


def fmt(value: Any) -> str:
    if value is None:
        return "—"
    if isinstance(value, float):
        return f"{value:,.2f}"
    if isinstance(value, int):
        return f"{value:,}"
    return str(value)


def delta(current: Any, reference: Any) -> tuple[str, float | None]:
    if not isinstance(current, (int, float)) or not isinstance(reference, (int, float)) or reference == 0:
        return "—", None
    rel = (current - reference) / reference
    return f"{rel:+.1%}", rel


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--history", default="docs/migration/metrics/results/history.ndjson")
    parser.add_argument("--label", help="report this label (default: last row in history)")
    args = parser.parse_args()

    rows = load_history(Path(args.history))
    if not rows:
        print("ERROR: history is empty", file=sys.stderr)
        return 1

    indices = [i for i, r in enumerate(rows) if args.label is None or r.get("snapshot.label") == args.label]
    if not indices:
        print(f"ERROR: no row with label {args.label}", file=sys.stderr)
        return 1
    current_index = indices[-1]
    current = rows[current_index]
    baseline = next((r for r in rows if r.get("snapshot.label") == "baseline"), None)
    previous = next(
        (r for r in reversed(rows[:current_index]) if str(r.get("snapshot.label", "")).startswith(MILESTONE_PREFIXES)),
        None,
    )

    print(f"## Metrics report — `{current.get('snapshot.label')}` @ `{str(current.get('git.commit'))[:10]}`\n")
    print(f"- Baseline: {'`' + str(baseline.get('git.commit'))[:10] + '`' if baseline else 'missing'}")
    print(f"- Previous milestone: {'`' + str(previous.get('snapshot.label')) + '` @ `' + str(previous.get('git.commit'))[:10] + '`' if previous else 'none'}")
    print(f"- Groups measured: {', '.join(current.get('snapshot.groups', [])) or 'static only'}")
    skipped = current.get("snapshot.skipped_fragments") or []
    if skipped:
        print(f"- Skipped stale fragments: {'; '.join(skipped)}")

    warnings = []
    for key in ENVIRONMENT_KEYS:
        for name, ref in (("baseline", baseline), ("previous", previous)):
            if ref and key in current and key in ref and current[key] != ref[key]:
                warnings.append(f"`{key}` differs from {name} (`{ref[key]}` → `{current[key]}`); runtime/build deltas are not comparable")
    for warning in warnings:
        print(f"- ⚠️ {warning}")

    regressions = []
    print("\n| Metric | Baseline | Previous | Current | Δ baseline | Δ previous | Threshold |")
    print("|---|---|---|---|---|---|---|")
    for key, (threshold, label) in THRESHOLDS.items():
        cur = current.get(key)
        base = baseline.get(key) if baseline else None
        prev = previous.get(key) if previous else None
        d_base, rel_base = delta(cur, base)
        d_prev, rel_prev = delta(cur, prev)
        flagged = any(rel is not None and rel > threshold for rel in (rel_base, rel_prev))
        if key == "coverage.excluded_annotation_count" and isinstance(cur, int) and isinstance(prev, int) and cur > prev:
            flagged = True
        if flagged:
            regressions.append(label)
        print(f"| {label}{' ❌' if flagged else ''} | {fmt(base)} | {fmt(prev)} | {fmt(cur)} | {d_base} | {d_prev} | +{threshold:.0%} |")
    for key, label in INFORMATIONAL.items():
        cur = current.get(key)
        base = baseline.get(key) if baseline else None
        prev = previous.get(key) if previous else None
        print(f"| {label} | {fmt(base)} | {fmt(prev)} | {fmt(cur)} | {delta(cur, base)[0]} | {delta(cur, prev)[0]} | info |")

    if regressions and not warnings:
        print(f"\n**Regressions beyond threshold:** {', '.join(regressions)}")
        return 1
    if regressions:
        print(f"\n**Possible regressions (environment differs, re-measure before acting):** {', '.join(regressions)}")
        return 1
    print("\nNo regressions beyond thresholds.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
