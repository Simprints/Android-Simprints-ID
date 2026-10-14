#!/usr/bin/env python3
"""Parse benchmark module startup test results and emit startup metric fragment JSON."""

from __future__ import annotations

import argparse
import json
import re
from collections import defaultdict
from pathlib import Path
from typing import Any


BENCHMARK_FILE_PATTERNS = (
    "**/*benchmarkData.json",
    "**/*benchmarkData*.json",
)


def safe_key(raw: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", raw.strip().lower()).strip("_")


def to_float(value: Any) -> float | None:
    if isinstance(value, (int, float)):
        return float(value)
    return None


def extract_runs(metric_payload: Any) -> list[float]:
    if isinstance(metric_payload, list):
        return [v for value in metric_payload if (v := to_float(value)) is not None]
    if not isinstance(metric_payload, dict):
        return []

    for key in ("runs", "samples", "values", "data"):
        value = metric_payload.get(key)
        if isinstance(value, list):
            return [v for item in value if (v := to_float(item)) is not None]
    return []


def extract_mean(metric_payload: Any) -> float | None:
    if not isinstance(metric_payload, dict):
        return None
    for key in ("mean", "average", "avg", "median", "p50"):
        value = to_float(metric_payload.get(key))
        if value is not None:
            return value
    return None


def benchmark_entries(payload: Any) -> list[dict[str, Any]]:
    if isinstance(payload, dict):
        benchmarks = payload.get("benchmarks")
        if isinstance(benchmarks, list):
            return [item for item in benchmarks if isinstance(item, dict)]
        if "metrics" in payload and isinstance(payload["metrics"], dict):
            return [payload]
    if isinstance(payload, list):
        return [item for item in payload if isinstance(item, dict)]
    return []


def parse_file(path: Path) -> dict[str, list[float]]:
    payload = json.loads(path.read_text(encoding="utf-8"))
    by_metric: dict[str, list[float]] = defaultdict(list)
    for benchmark in benchmark_entries(payload):
        metrics = benchmark.get("metrics")
        if not isinstance(metrics, dict):
            continue
        for raw_metric_name, metric_payload in metrics.items():
            metric_name = str(raw_metric_name).strip()
            if not metric_name:
                continue
            runs = extract_runs(metric_payload)
            if runs:
                by_metric[metric_name].extend(runs)
                continue
            mean = extract_mean(metric_payload)
            if mean is not None:
                by_metric[metric_name].append(mean)
    return by_metric


def parse_results(input_dir: Path) -> dict[str, float]:
    files: list[Path] = []
    for pattern in BENCHMARK_FILE_PATTERNS:
        files.extend(input_dir.glob(pattern))
    files = sorted(set(path.resolve() for path in files if path.is_file()))
    if not files:
        raise FileNotFoundError(
            f"No benchmark JSON files found under {input_dir}. "
            "Run :benchmark:connectedBenchmarkAndroidTest first."
        )

    by_metric: dict[str, list[float]] = defaultdict(list)
    for file_path in files:
        parsed = parse_file(file_path)
        for metric_name, runs in parsed.items():
            by_metric[metric_name].extend(runs)

    startup_metric_names = [
        name
        for name in by_metric.keys()
        if "startup" in name.lower() or "display" in name.lower()
    ]

    metric_names = startup_metric_names if startup_metric_names else list(by_metric.keys())

    result: dict[str, float] = {}
    for metric_name in sorted(metric_names):
        runs = by_metric.get(metric_name, [])
        if not runs:
            continue
        safe_metric = safe_key(metric_name)
        sample_count = len(runs)
        avg = sum(runs) / sample_count
        sorted_runs = sorted(runs)
        mid = sample_count // 2
        median = (
            sorted_runs[mid]
            if sample_count % 2 == 1
            else (sorted_runs[mid - 1] + sorted_runs[mid]) / 2.0
        )
        result[f"startup_time.{safe_metric}_avg"] = round(avg, 4)
        result[f"startup_time.{safe_metric}_median"] = round(median, 4)
        result[f"startup_time.{safe_metric}_min"] = round(sorted_runs[0], 4)
        result[f"startup_time.{safe_metric}_max"] = round(sorted_runs[-1], 4)
        result[f"startup_time.{safe_metric}_sample_count"] = float(sample_count)
    return result


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--input-dir",
        default="benchmark/build/outputs/connected_android_test_additional_output",
    )
    parser.add_argument(
        "--output-json",
        default="docs/migration/metrics/results/fragments/startup_time.json",
    )
    args = parser.parse_args()

    input_dir = Path(args.input_dir).resolve()
    output_json = Path(args.output_json).resolve()
    output_json.parent.mkdir(parents=True, exist_ok=True)

    metrics = parse_results(input_dir)
    output_json.write_text(json.dumps(metrics, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(f"Wrote {output_json}")


if __name__ == "__main__":
    main()
