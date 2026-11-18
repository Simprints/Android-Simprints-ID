#!/usr/bin/env python3
"""Parse a gradle-profiler `--csv-format long` benchmark CSV into a build_time metrics fragment.

Expected columns: Scenario, Tool, Tasks, Phase, Iteration, Sample, Duration, Count.
Only `MEASURE` rows of the `total execution time` sample are used; warm-up iterations are ignored.
gradle-profiler reports durations in milliseconds; the fragment stores seconds.
"""

from __future__ import annotations

import argparse
import csv
import json
import statistics
import sys
from pathlib import Path

REQUIRED_COLUMNS = {"Scenario", "Tool", "Phase", "Sample", "Duration"}


def safe_name(raw: str) -> str:
    return "_".join(raw.strip().lower().split())


def parse(csv_path: Path) -> dict[str, object]:
    with csv_path.open("r", encoding="utf-8", newline="") as f:
        reader = csv.DictReader(f)
        missing = REQUIRED_COLUMNS - set(reader.fieldnames or [])
        if missing:
            raise ValueError(f"{csv_path} is not a long-format gradle-profiler CSV (missing {sorted(missing)})")

        durations_ms: dict[str, list[float]] = {}
        tools: set[str] = set()
        for row in reader:
            if row["Phase"].strip() != "MEASURE" or row["Sample"].strip() != "total execution time":
                continue
            durations_ms.setdefault(safe_name(row["Scenario"]), []).append(float(row["Duration"]))
            tools.add(row["Tool"].strip())

    if not durations_ms:
        raise ValueError(f"No MEASURE rows found in {csv_path}")

    metrics: dict[str, object] = {"build_time.tool": ", ".join(sorted(tools))}
    for scenario, values in sorted(durations_ms.items()):
        seconds = [v / 1000.0 for v in values]
        prefix = f"build_time.{scenario}"
        metrics[f"{prefix}_seconds_mean"] = round(statistics.fmean(seconds), 3)
        metrics[f"{prefix}_seconds_median"] = round(statistics.median(seconds), 3)
        metrics[f"{prefix}_seconds_min"] = round(min(seconds), 3)
        metrics[f"{prefix}_seconds_max"] = round(max(seconds), 3)
        metrics[f"{prefix}_sample_count"] = len(seconds)
    return metrics


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("csv_path")
    parser.add_argument("output_json")
    args = parser.parse_args()

    try:
        metrics = parse(Path(args.csv_path).resolve())
    except (OSError, ValueError) as e:
        print(f"ERROR: {e}", file=sys.stderr)
        return 1

    output_json = Path(args.output_json).resolve()
    output_json.parent.mkdir(parents=True, exist_ok=True)
    # Always a fresh fragment: keys from earlier runs must not survive.
    output_json.write_text(json.dumps(metrics, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(f"Wrote {output_json}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
