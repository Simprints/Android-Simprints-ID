#!/usr/bin/env python3
"""Collect Compose migration metrics and write a unified snapshot/history."""

from __future__ import annotations

import argparse
import csv
import datetime as dt
import json
import re
import subprocess
from pathlib import Path
from typing import Any

FEATURE_PATTERN = re.compile(r'":feature:[^"]+"')
INTEROP_PATTERNS = {
    "compose_view": re.compile(r"\bComposeView\b"),
    "android_view": re.compile(r"\bAndroidView\b"),
}
EXCLUDED_COVERAGE_PATTERN = re.compile(r"@ExcludedFromGeneratedTestCoverageReports\b")


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def git_value(repo_root: Path, args: list[str]) -> str | None:
    try:
        result = subprocess.run(
            ["git", *args],
            cwd=repo_root,
            capture_output=True,
            text=True,
            check=True,
        )
        return result.stdout.strip() or None
    except Exception:
        return None


def parse_feature_modules(settings_path: Path) -> list[str]:
    content = read_text(settings_path)
    modules = sorted(set(m.strip('"') for m in FEATURE_PATTERN.findall(content)))
    return modules


def parse_status_csv(status_csv_path: Path) -> dict[str, str]:
    if not status_csv_path.exists():
        return {}
    status_by_module: dict[str, str] = {}
    with status_csv_path.open("r", encoding="utf-8", newline="") as f:
        reader = csv.DictReader(f)
        for row in reader:
            module = (row.get("module") or "").strip()
            status = (row.get("status") or "").strip().lower()
            if module:
                status_by_module[module] = status
    return status_by_module


def count_in_files(repo_root: Path, pattern: re.Pattern[str], suffix: str) -> int:
    count = 0
    for path in repo_root.rglob(f"*{suffix}"):
        if any(part in {"build", ".git", ".gradle"} for part in path.parts):
            continue
        try:
            count += len(pattern.findall(path.read_text(encoding="utf-8", errors="ignore")))
        except OSError:
            continue
    return count


def load_fragments(fragment_dir: Path) -> dict[str, Any]:
    merged: dict[str, Any] = {}
    if not fragment_dir.exists():
        return merged
    for fragment_path in sorted(fragment_dir.glob("*.json")):
        try:
            data = json.loads(fragment_path.read_text(encoding="utf-8"))
            if isinstance(data, dict):
                merged.update(data)
        except json.JSONDecodeError:
            continue
    return merged


def build_metrics(repo_root: Path, status_csv: Path, fragment_dir: Path) -> dict[str, Any]:
    settings_path = repo_root / "settings.gradle.kts"
    feature_modules = parse_feature_modules(settings_path)
    status_by_module = parse_status_csv(status_csv)

    migrated_modules = [
        module
        for module in feature_modules
        if status_by_module.get(module) in {"compose", "migrated"}
    ]

    interop_counts = {
        name: count_in_files(repo_root, pattern, ".kt")
        for name, pattern in INTEROP_PATTERNS.items()
    }
    interop_total = interop_counts["compose_view"] + interop_counts["android_view"]

    excluded_coverage_annotation_count = count_in_files(
        repo_root, EXCLUDED_COVERAGE_PATTERN, ".kt"
    )

    total_feature_modules = len(feature_modules)
    conversion_pct = (
        (len(migrated_modules) / total_feature_modules) * 100.0
        if total_feature_modules > 0
        else 0.0
    )

    now = dt.datetime.now(dt.timezone.utc).isoformat()
    commit = git_value(repo_root, ["rev-parse", "HEAD"])
    branch = git_value(repo_root, ["rev-parse", "--abbrev-ref", "HEAD"])

    metrics: dict[str, Any] = {
        "timestamp_utc": now,
        "git.commit": commit,
        "git.branch": branch,
        "module.total_feature_modules": total_feature_modules,
        "module.migrated_feature_modules": len(migrated_modules),
        "module.conversion_pct": round(conversion_pct, 2),
        "interop.compose_view_nodes": interop_counts["compose_view"],
        "interop.android_view_nodes": interop_counts["android_view"],
        "interop.total_nodes": interop_total,
        "coverage.excluded_annotation_count": excluded_coverage_annotation_count,
    }

    metrics.update(load_fragments(fragment_dir))
    return metrics


def append_history(history_path: Path, row: dict[str, Any]) -> None:
    history_path.parent.mkdir(parents=True, exist_ok=True)
    with history_path.open("a", encoding="utf-8") as f:
        f.write(json.dumps(row, sort_keys=True))
        f.write("\n")


def write_latest(latest_path: Path, row: dict[str, Any]) -> None:
    latest_path.parent.mkdir(parents=True, exist_ok=True)
    latest_path.write_text(json.dumps(row, indent=2, sort_keys=True) + "\n", encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--repo-root", default=".")
    parser.add_argument(
        "--status-csv",
        default="docs/migration/metrics/compose-module-status.csv",
    )
    parser.add_argument(
        "--fragment-dir",
        default="docs/migration/metrics/results/fragments",
    )
    parser.add_argument(
        "--latest-output",
        default="docs/migration/metrics/results/latest.json",
    )
    parser.add_argument(
        "--history-output",
        default="docs/migration/metrics/results/history.ndjson",
    )
    args = parser.parse_args()

    repo_root = Path(args.repo_root).resolve()
    status_csv = (repo_root / args.status_csv).resolve()
    fragment_dir = (repo_root / args.fragment_dir).resolve()
    latest_output = (repo_root / args.latest_output).resolve()
    history_output = (repo_root / args.history_output).resolve()

    metrics = build_metrics(repo_root, status_csv, fragment_dir)
    write_latest(latest_output, metrics)
    append_history(history_output, metrics)

    print(f"Wrote {latest_output}")
    print(f"Appended {history_output}")


if __name__ == "__main__":
    main()
