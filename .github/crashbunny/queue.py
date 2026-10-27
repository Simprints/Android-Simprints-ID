#!/usr/bin/env python3
"""State handling for the nightly CrashBunny workflow.

All state lives as JSON files in a checkout of the `crashbunny-state` branch:
  state.json   - last triaged window end and a per-issue ledger
  queue.json   - tonight's triage result and the fix queue
Stdlib only, so it runs on a bare GitHub runner.
"""
import argparse
import json
import re
import sys
from datetime import datetime, timedelta, timezone
from pathlib import Path

FIX_VERDICTS = {"app_bug", "regression"}
IMPACT_ORDER = {"high": 0, "medium": 1, "low": 2}
DEFAULT_LOOKBACK = timedelta(hours=24)
# A "night" is keyed by the morning it is reported on, so runs from 22:00 UTC onwards count towards the next day
NIGHT_SHIFT = timedelta(hours=6)

SESSION_LIMIT_PATTERNS = [r"hit your session limit", r"5-hour limit", r"usage limit reached"]
WEEKLY_LIMIT_PATTERNS = [r"hit your weekly limit"]


def now():
    return datetime.now(timezone.utc)


def iso(dt):
    return dt.strftime("%Y-%m-%dT%H:%M:%SZ")


def night_id(dt):
    return (dt + NIGHT_SHIFT).date().isoformat()


def load(path, default):
    return json.loads(path.read_text()) if path.exists() else default


def save(path, data):
    path.write_text(json.dumps(data, indent=2, sort_keys=True) + "\n")


def paths(state_dir):
    d = Path(state_dir)
    return d / "state.json", d / "queue.json"


def gh_output(**values):
    for key, value in values.items():
        print(f"{key}={value}")


def cmd_phase(args):
    """Decide what this invocation should do: triage, fix or idle."""
    state_path, queue_path = paths(args.state_dir)
    state = load(state_path, {})
    queue = load(queue_path, {})
    current = night_id(now())
    if queue.get("night_id") != current:
        start = state.get("last_window_end") or iso(now() - DEFAULT_LOOKBACK)
        gh_output(phase="triage", night_id=current, window_start=start, window_end=iso(now()))
        return
    pending = [i for i in queue["items"] if i["status"] in ("pending", "in_progress")]
    if queue.get("weekly_limit_hit") and not args.ignore_weekly_limit:
        gh_output(phase="report" if not queue.get("reported") else "idle")
    elif pending:
        gh_output(phase="fix")
    elif not queue.get("reported"):
        gh_output(phase="report")
    else:
        gh_output(phase="idle")


def cmd_init(args):
    """Turn a triage result into tonight's queue, carrying over unfinished items."""
    state_path, queue_path = paths(args.state_dir)
    state = load(state_path, {"issues": {}})
    previous = load(queue_path, {})
    triage = json.loads(Path(args.triage).read_text())
    for key in ("tiers", "issues"):
        if key not in triage:
            sys.exit(f"triage result is missing '{key}'")

    items = {}
    for item in previous.get("items", []):
        if item["status"] in ("pending", "in_progress"):
            item["status"] = "pending"
            item["carried_over"] = True
            items[item["root_cause_id"]] = item

    for issue in triage["issues"]:
        state["issues"][issue["crashlytics_issue_id"]] = {
            "root_cause_id": issue.get("root_cause_id"),
            "verdict": issue.get("verdict"),
            "last_seen": args.window_end,
            "title": issue.get("title"),
        }
        if issue.get("verdict") not in FIX_VERDICTS or not issue.get("root_cause_id"):
            continue
        item = items.setdefault(issue["root_cause_id"], {
            "root_cause_id": issue["root_cause_id"],
            "root_cause": issue.get("root_cause"),
            "impact": issue.get("impact"),
            "issue_ids": [],
            "status": "pending",
            "attempts": 0,
        })
        if issue["crashlytics_issue_id"] not in item["issue_ids"]:
            item["issue_ids"].append(issue["crashlytics_issue_id"])
        if IMPACT_ORDER.get(issue.get("impact"), 3) < IMPACT_ORDER.get(item.get("impact"), 3):
            item["impact"] = issue["impact"]

    queue = {
        "night_id": args.night_id,
        "window_start": args.window_start,
        "window_end": args.window_end,
        "dry_run": args.dry_run == "true",
        "triage": triage,
        "items": sorted(items.values(), key=lambda i: IMPACT_ORDER.get(i.get("impact"), 3)),
        "reported": False,
        "weekly_limit_hit": False,
    }
    state["last_window_end"] = args.window_end
    save(queue_path, queue)
    save(state_path, state)
    print(f"Queued {len(queue['items'])} fix item(s) from {len(triage['issues'])} issue(s)")


def cmd_next(args):
    """Claim the next pending item and write it for the fix prompt."""
    _, queue_path = paths(args.state_dir)
    queue = load(queue_path, {})
    done = sum(1 for i in queue.get("items", []) if i["status"] == "done" and i.get("pr_url"))
    if done >= args.max_prs:
        gh_output(item="")
        return
    for item in queue.get("items", []):
        if item["status"] == "pending" and item["attempts"] < args.max_attempts:
            item["status"] = "in_progress"
            item["attempts"] += 1
            issues = [i for i in queue["triage"]["issues"] if i["crashlytics_issue_id"] in item["issue_ids"]]
            Path(args.out).write_text(json.dumps({**item, "issues": issues, "dry_run": queue["dry_run"]}, indent=2))
            save(queue_path, queue)
            gh_output(item=item["root_cause_id"])
            return
    gh_output(item="")


def classify_log(log_text):
    # Only look at the final result, system events and plain stderr lines, so a limit message quoted in
    # a tool output (e.g. a file Claude read) can't be mistaken for a real one
    relevant = []
    for line in log_text.splitlines():
        try:
            event = json.loads(line)
        except ValueError:
            relevant.append(line)
            continue
        if isinstance(event, dict) and event.get("type") in ("result", "system"):
            relevant.append(json.dumps(event))
    text = "\n".join(relevant)
    if any(re.search(p, text, re.IGNORECASE) for p in WEEKLY_LIMIT_PATTERNS):
        return "weekly"
    if any(re.search(p, text, re.IGNORECASE) for p in SESSION_LIMIT_PATTERNS):
        return "session"
    return "none"


def cmd_limit(args):
    """Tell whether a Claude Code run log ended on a usage limit."""
    log_text = Path(args.log).read_text(errors="replace") if Path(args.log).exists() else ""
    gh_output(limit=classify_log(log_text))


def cmd_complete(args):
    """Record the outcome of one fix attempt."""
    _, queue_path = paths(args.state_dir)
    queue = load(queue_path, {})
    item = next(i for i in queue["items"] if i["root_cause_id"] == args.id)
    if args.limit in ("session", "weekly"):
        # The attempt never finished, so it does not count against the item
        item["status"] = "pending"
        item["attempts"] -= 1
        queue["weekly_limit_hit"] = queue.get("weekly_limit_hit") or args.limit == "weekly"
    else:
        result_path = Path(args.result)
        result = json.loads(result_path.read_text()) if result_path.exists() else {
            "status": "failed", "reason": "Fix run ended without writing a result",
        }
        item["fix"] = result
        item["pr_url"] = result.get("pr_url")
        item["status"] = "done" if result.get("status") in ("pr_opened", "would_fix", "skipped_existing") else "failed"
    save(queue_path, queue)


def cmd_report(args):
    """Build the payload for the Simphony job."""
    _, queue_path = paths(args.state_dir)
    queue = load(queue_path, {})
    fixes = {}
    for item in queue.get("items", []):
        fix = item.get("fix") or {"status": "pending" if item["status"] in ("pending", "in_progress") else "failed"}
        for issue_id in item["issue_ids"]:
            fixes[issue_id] = fix
    pending = sum(1 for i in queue.get("items", []) if i["status"] in ("pending", "in_progress"))
    if queue.get("weekly_limit_hit"):
        status = "paused_weekly_limit"
    else:
        status = "in_progress" if pending else "drained"
    report = {
        "run": {
            "window_start": queue.get("window_start"),
            "window_end": queue.get("window_end"),
            "dry_run": queue.get("dry_run", True),
            "workflow_run_url": args.run_url,
            "queue_status": status,
            "pending": pending,
        },
        "tiers": queue.get("triage", {}).get("tiers", []),
        "issues": [
            {**issue, "fix": fixes.get(issue["crashlytics_issue_id"])}
            for issue in queue.get("triage", {}).get("issues", [])
        ],
    }
    Path(args.out).write_text(json.dumps(report, indent=2))


def cmd_mark_reported(args):
    _, queue_path = paths(args.state_dir)
    queue = load(queue_path, {})
    queue["reported"] = True
    save(queue_path, queue)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--state-dir", required=True)
    sub = parser.add_subparsers(dest="command", required=True)

    phase = sub.add_parser("phase")
    phase.add_argument("--ignore-weekly-limit", action="store_true")
    phase.set_defaults(func=cmd_phase)

    init = sub.add_parser("init")
    init.add_argument("--triage", required=True)
    init.add_argument("--night-id", required=True)
    init.add_argument("--window-start", required=True)
    init.add_argument("--window-end", required=True)
    init.add_argument("--dry-run", choices=["true", "false"], required=True)
    init.set_defaults(func=cmd_init)

    nxt = sub.add_parser("next")
    nxt.add_argument("--out", required=True)
    nxt.add_argument("--max-prs", type=int, default=15)
    nxt.add_argument("--max-attempts", type=int, default=2)
    nxt.set_defaults(func=cmd_next)

    limit = sub.add_parser("limit")
    limit.add_argument("--log", required=True)
    limit.set_defaults(func=cmd_limit)

    complete = sub.add_parser("complete")
    complete.add_argument("--id", required=True)
    complete.add_argument("--result", required=True)
    complete.add_argument("--limit", default="none")
    complete.set_defaults(func=cmd_complete)

    report = sub.add_parser("report")
    report.add_argument("--run-url", required=True)
    report.add_argument("--out", required=True)
    report.set_defaults(func=cmd_report)

    mark = sub.add_parser("mark-reported")
    mark.set_defaults(func=cmd_mark_reported)

    args = parser.parse_args()
    args.func(args)


if __name__ == "__main__":
    main()
