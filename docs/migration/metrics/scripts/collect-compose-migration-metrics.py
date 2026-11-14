#!/usr/bin/env python3
"""Collect Compose migration metrics and write a unified snapshot/history row.

Static metrics (module status, interop nodes, coverage) are computed from the working tree.
Runtime and build metrics are read from fragment files written by the run-*.sh scripts. Each
fragment must be stamped with the commit it was measured on (`<group>.git_commit`); fragments
from another commit are rejected so that one snapshot never mixes measurements from different
code states.
"""

from __future__ import annotations

import argparse
import datetime as dt
import json
import re
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import Any

STATUS_BEGIN = "<!-- module-status:begin -->"
STATUS_END = "<!-- module-status:end -->"
PHASE1_VALUES = {"not-started", "audit", "planned", "in-progress", "done", "n/a"}
PHASE2_VALUES = {"not-started", "planned", "in-progress", "validated", "done", "n/a"}
LABEL_PATTERN = re.compile(r"^(baseline|pre-compose|adhoc|p1:[a-z0-9-]+|p2-flag:[a-z0-9-]+|p2:[a-z0-9-]+)$")

# Call sites only: declarations of these functions, imports and comments are skipped line by line.
DECLARATION_PATTERN = re.compile(r"\bfun\s+(?:[\w<>?, ]+\.)?(ComposeView|simComposeView|AndroidView)\s*\(")
INTEROP_PATTERNS = {
    "compose_view": re.compile(r"\b(ComposeView|simComposeView)\s*\("),
    "android_view": re.compile(r"\bAndroidView\s*\("),
}
EXCLUDED_COVERAGE_PATTERN = re.compile(r"@ExcludedFromGeneratedTestCoverageReports\b")
SKIPPED_DIRS = {"build", ".git", ".gradle", "build-logic", "docs"}
RESULTS_DIR = "docs/migration/metrics/results"


class MetricsError(Exception):
    pass


def git_value(repo_root: Path, args: list[str]) -> str | None:
    try:
        result = subprocess.run(["git", *args], cwd=repo_root, capture_output=True, text=True, check=True)
        return result.stdout.strip() or None
    except (OSError, subprocess.CalledProcessError):
        return None


def git_dirty_paths(repo_root: Path) -> list[str]:
    # Not git_value(): stripping the output would cut the status column of the first line.
    output = subprocess.run(
        ["git", "status", "--porcelain"], cwd=repo_root, capture_output=True, text=True, check=True
    ).stdout
    paths = [line[3:] for line in output.splitlines() if line]
    # Metric outputs are expected to change while collecting.
    return [p for p in paths if not p.startswith(RESULTS_DIR)]


def parse_catalog(catalog_path: Path) -> list[dict[str, str]]:
    content = catalog_path.read_text(encoding="utf-8")
    if STATUS_BEGIN not in content or STATUS_END not in content:
        raise MetricsError(f"Status markers not found in {catalog_path}")
    block = content.split(STATUS_BEGIN, 1)[1].split(STATUS_END, 1)[0]

    rows: list[dict[str, str]] = []
    for line in block.splitlines():
        cells = [c.strip() for c in line.strip().strip("|").split("|")]
        if len(cells) < 4 or not cells[0].startswith("`:"):
            continue
        module, wave, phase1, phase2 = cells[0].strip("`"), cells[1], cells[2], cells[3]
        if phase1 not in PHASE1_VALUES:
            raise MetricsError(f"{module}: invalid Phase 1 status '{phase1}'")
        if phase2 not in PHASE2_VALUES:
            raise MetricsError(f"{module}: invalid Phase 2 status '{phase2}'")
        rows.append({"module": module, "wave": wave, "phase1": phase1, "phase2": phase2})
    if not rows:
        raise MetricsError(f"No module rows found in {catalog_path}")
    return rows


def settings_modules(repo_root: Path) -> set[str]:
    content = (repo_root / "settings.gradle.kts").read_text(encoding="utf-8")
    return set(re.findall(r'"(:[a-z0-9:-]+)"', content))


def module_metrics(rows: list[dict[str, str]]) -> dict[str, Any]:
    p1_scope = [r for r in rows if r["phase1"] != "n/a"]
    p2_scope = [r for r in rows if r["phase2"] != "n/a"]
    p1_done = [r for r in p1_scope if r["phase1"] == "done"]
    p2_done = [r for r in p2_scope if r["phase2"] == "done"]
    p2_interop = [r for r in p2_scope if r["phase2"] in {"in-progress", "validated"}]

    def pct(part: int, total: int) -> float:
        return round(part / total * 100.0, 2) if total else 0.0

    return {
        "module.catalog_rows": len(rows),
        "module.phase1_scope": len(p1_scope),
        "module.phase1_done": len(p1_done),
        "module.phase1_done_pct": pct(len(p1_done), len(p1_scope)),
        "module.phase2_scope": len(p2_scope),
        "module.compose_modules": len(p2_done),
        "module.interop_modules": len(p2_interop),
        "module.legacy_modules": len(p2_scope) - len(p2_done) - len(p2_interop),
        "module.conversion_pct": pct(len(p2_done), len(p2_scope)),
    }


def main_kotlin_files(repo_root: Path):
    for path in repo_root.rglob("*.kt"):
        rel = path.relative_to(repo_root)
        if any(part in SKIPPED_DIRS for part in rel.parts):
            continue
        # Production code only: src/main (and flavour/build-type source sets), never tests.
        parts = rel.parts
        if "src" not in parts:
            continue
        source_set = parts[parts.index("src") + 1] if parts.index("src") + 1 < len(parts) else ""
        if "test" in source_set.lower():
            continue
        yield path


def count_call_sites(files: list[Path], pattern: re.Pattern[str]) -> int:
    count = 0
    for path in files:
        try:
            lines = path.read_text(encoding="utf-8", errors="ignore").splitlines()
        except OSError:
            continue
        for line in lines:
            stripped = line.strip()
            if stripped.startswith(("import ", "//", "*", "/*")) or DECLARATION_PATTERN.search(stripped):
                continue
            count += len(pattern.findall(line))
    return count


def jacoco_metrics(repo_root: Path) -> dict[str, Any]:
    reports = [
        p for p in repo_root.glob("**/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        if ".git" not in p.parts
    ]
    if not reports:
        raise MetricsError("--with-jacoco set but no jacocoTestReport.xml found; run ./gradlew jacocoTestReport first")
    head_time = git_value(repo_root, ["log", "-1", "--format=%ct"])
    missed = covered = 0
    stale = []
    for report in reports:
        if head_time and report.stat().st_mtime < int(head_time):
            stale.append(str(report.relative_to(repo_root)))
        root = ET.parse(report).getroot()
        for counter in root.findall("counter"):
            if counter.get("type") == "LINE":
                missed += int(counter.get("missed", 0))
                covered += int(counter.get("covered", 0))
    if stale:
        raise MetricsError(f"JaCoCo reports older than HEAD (re-run jacocoTestReport): {', '.join(stale[:5])}")
    total = missed + covered
    return {
        "coverage.jacoco_reports": len(reports),
        "coverage.line_covered": covered,
        "coverage.line_missed": missed,
        "coverage.line_pct": round(covered / total * 100.0, 2) if total else 0.0,
    }


def load_fragments(fragment_dir: Path, head: str | None, allow_stale: bool) -> tuple[dict[str, Any], list[str]]:
    merged: dict[str, Any] = {}
    skipped: list[str] = []
    if not fragment_dir.exists():
        return merged, skipped
    for fragment_path in sorted(fragment_dir.glob("*.json")):
        group = fragment_path.stem
        try:
            data = json.loads(fragment_path.read_text(encoding="utf-8"))
        except json.JSONDecodeError as e:
            raise MetricsError(f"Invalid JSON in {fragment_path}: {e}") from e
        if not isinstance(data, dict):
            raise MetricsError(f"Fragment {fragment_path} is not a JSON object")
        commit = data.get(f"{group}.git_commit")
        if commit != head:
            message = f"{fragment_path.name} measured on {commit or 'unknown commit'}, HEAD is {head}"
            if not allow_stale:
                raise MetricsError(f"Stale fragment: {message}. Re-run its script or pass --allow-stale.")
            skipped.append(message)
            continue
        merged.update(data)
    return merged, skipped


def build_metrics(args: argparse.Namespace, repo_root: Path) -> dict[str, Any]:
    head = git_value(repo_root, ["rev-parse", "HEAD"])
    dirty = git_dirty_paths(repo_root)
    if dirty and not args.allow_dirty:
        raise MetricsError(f"Working tree has uncommitted changes ({', '.join(dirty[:5])}); commit or pass --allow-dirty.")

    rows = parse_catalog((repo_root / args.catalog).resolve())
    unknown = sorted({r["module"] for r in rows} - settings_modules(repo_root))
    if unknown:
        raise MetricsError(f"Catalog modules missing from settings.gradle.kts: {', '.join(unknown)}")

    files = list(main_kotlin_files(repo_root))
    interop = {name: count_call_sites(files, pattern) for name, pattern in INTEROP_PATTERNS.items()}

    metrics: dict[str, Any] = {
        "snapshot.label": args.label,
        "snapshot.timestamp_utc": dt.datetime.now(dt.timezone.utc).isoformat(timespec="seconds"),
        "git.commit": head,
        "git.branch": git_value(repo_root, ["rev-parse", "--abbrev-ref", "HEAD"]),
        "git.dirty": bool(dirty),
        **module_metrics(rows),
        "interop.compose_view_nodes": interop["compose_view"],
        "interop.android_view_nodes": interop["android_view"],
        "interop.total_nodes": interop["compose_view"] + interop["android_view"],
        "coverage.excluded_annotation_count": count_call_sites(files, EXCLUDED_COVERAGE_PATTERN),
    }
    if args.with_jacoco:
        metrics.update(jacoco_metrics(repo_root))

    fragments, skipped = load_fragments((repo_root / args.fragment_dir).resolve(), head, args.allow_stale)
    metrics.update(fragments)
    metrics["snapshot.groups"] = sorted({k.split(".", 1)[0] for k in fragments})
    metrics["snapshot.skipped_fragments"] = skipped
    return metrics


def history_has(history_path: Path, label: str, commit: str | None) -> bool:
    if not history_path.exists():
        return False
    for line in history_path.read_text(encoding="utf-8").splitlines():
        if not line.strip():
            continue
        row = json.loads(line)
        if row.get("snapshot.label") == label and row.get("git.commit") == commit:
            return True
    return False


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--label", required=True, help="baseline | pre-compose | adhoc | p1:<module> | p2-flag:<module> | p2:<module>")
    parser.add_argument("--repo-root", default=".")
    parser.add_argument("--catalog", default="docs/migration/migration-catalog.md")
    parser.add_argument("--fragment-dir", default=f"{RESULTS_DIR}/fragments")
    parser.add_argument("--latest-output", default=f"{RESULTS_DIR}/latest.json")
    parser.add_argument("--history-output", default=f"{RESULTS_DIR}/history.ndjson")
    parser.add_argument("--with-jacoco", action="store_true", help="include line coverage from jacocoTestReport.xml files")
    parser.add_argument("--allow-stale", action="store_true", help="skip fragments from another commit instead of failing")
    parser.add_argument("--allow-dirty", action="store_true", help="allow uncommitted changes (never for milestone labels)")
    parser.add_argument("--dry-run", action="store_true", help="print the snapshot without writing files")
    parser.add_argument("--force", action="store_true", help="append even if this label/commit already exists in history")
    args = parser.parse_args()

    if not LABEL_PATTERN.match(args.label):
        print(f"ERROR: invalid label '{args.label}'", file=sys.stderr)
        return 2
    if args.label != "adhoc" and (args.allow_dirty or args.allow_stale):
        print("ERROR: --allow-dirty/--allow-stale are only allowed with --label adhoc", file=sys.stderr)
        return 2

    repo_root = Path(args.repo_root).resolve()
    try:
        metrics = build_metrics(args, repo_root)
    except MetricsError as e:
        print(f"ERROR: {e}", file=sys.stderr)
        return 1

    if args.dry_run:
        print(json.dumps(metrics, indent=2, sort_keys=True))
        return 0

    history_path = (repo_root / args.history_output).resolve()
    if history_has(history_path, args.label, metrics["git.commit"]) and not args.force:
        print(f"ERROR: history already has label '{args.label}' for {metrics['git.commit']}; pass --force to append again", file=sys.stderr)
        return 1

    latest_path = (repo_root / args.latest_output).resolve()
    latest_path.parent.mkdir(parents=True, exist_ok=True)
    latest_path.write_text(json.dumps(metrics, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    with history_path.open("a", encoding="utf-8") as f:
        f.write(json.dumps(metrics, sort_keys=True) + "\n")

    print(f"Wrote {latest_path}")
    print(f"Appended {history_path} (label={args.label}, groups={metrics['snapshot.groups']})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
