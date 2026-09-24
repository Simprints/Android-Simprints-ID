#!/usr/bin/env python3
"""Parse gradle-profiler benchmark CSV and emit build_time metrics fragment JSON."""

from __future__ import annotations

import argparse
import csv
import json
from pathlib import Path


SCENARIO_COLUMNS = ("scenario", "benchmark", "name")
VALUE_COLUMNS = ("value", "total", "duration", "time", "mean", "median")
UNIT_COLUMNS = ("unit", "units")


def first_existing(header: list[str], candidates: tuple[str, ...]) -> str | None:
    lowered = {h.lower(): h for h in header}
    for candidate in candidates:
        if candidate in lowered:
            return lowered[candidate]
    return None


def to_seconds(value: float, unit: str | None) -> float:
    if unit is None:
        return value
    normalized = unit.strip().lower()
    if normalized in {"s", "sec", "secs", "second", "seconds"}:
        return value
    if normalized in {"ms", "millisecond", "milliseconds"}:
        return value / 1000.0
    if normalized in {"ns", "nanosecond", "nanoseconds"}:
        return value / 1_000_000_000.0
    return value


def parse(csv_path: Path) -> dict[str, float]:
    values_by_scenario: dict[str, list[float]] = {}
    with csv_path.open("r", encoding="utf-8", newline="") as f:
        reader = csv.DictReader(f)
        if not reader.fieldnames:
            return {}
        scenario_col = first_existing(reader.fieldnames, SCENARIO_COLUMNS)
        value_col = first_existing(reader.fieldnames, VALUE_COLUMNS)
        unit_col = first_existing(reader.fieldnames, UNIT_COLUMNS)
        if scenario_col is None or value_col is None:
            return {}

        for row in reader:
            scenario = (row.get(scenario_col) or "").strip()
            raw_value = (row.get(value_col) or "").strip()
            if not scenario or not raw_value:
                continue
            try:
                value = float(raw_value)
            except ValueError:
                continue
            unit = (row.get(unit_col) or "").strip() if unit_col else None
            values_by_scenario.setdefault(scenario, []).append(to_seconds(value, unit))

    metrics: dict[str, float] = {}
    for scenario, values in values_by_scenario.items():
        if not values:
            continue
        avg = sum(values) / len(values)
        safe_name = scenario.strip().lower().replace(" ", "_")
        metrics[f"build_time.{safe_name}_seconds_avg"] = round(avg, 4)
        metrics[f"build_time.{safe_name}_sample_count"] = float(len(values))
    return metrics


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("csv_path")
    parser.add_argument("output_json")
    args = parser.parse_args()

    csv_path = Path(args.csv_path).resolve()
    output_json = Path(args.output_json).resolve()
    output_json.parent.mkdir(parents=True, exist_ok=True)

    metrics = parse(csv_path)
    output_json.write_text(json.dumps(metrics, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(f"Wrote {output_json}")


if __name__ == "__main__":
    main()
