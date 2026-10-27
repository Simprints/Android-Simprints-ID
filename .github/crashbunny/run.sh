#!/usr/bin/env bash
# One invocation of the nightly CrashBunny workflow: triage (once per night), then work through the fix queue
# until it is empty or a Claude usage limit is hit, then report to Simphony when the night is done.
# State is committed to the `crashbunny-state` branch after every step, so the next hourly run picks up where
# this one stopped.
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(git -C "$HERE" rev-parse --show-toplevel)"
WORK="$RUNNER_TEMP/crashbunny"
STATE_DIR="$WORK/state"
QUEUE=(python3 "$HERE/queue.py" --state-dir "$STATE_DIR")
MAX_PRS="${CRASHBUNNY_MAX_PRS:-15}"
FINAL_HOUR_UTC="${CRASHBUNNY_FINAL_HOUR_UTC:-05}"
mkdir -p "$WORK/patches" "$WORK/logs" "$WORK/setup-files"

out() { sed -n "s/^$1=//p"; }

setup_state() {
    if git -C "$REPO" ls-remote --exit-code --heads origin crashbunny-state >/dev/null; then
        git -C "$REPO" fetch -q origin crashbunny-state
        git -C "$REPO" worktree add -q -B crashbunny-state "$STATE_DIR" origin/crashbunny-state
    else
        git -C "$REPO" worktree add -q --orphan -b crashbunny-state "$STATE_DIR"
    fi
}

save_state() {
    git -C "$STATE_DIR" add -A
    if ! git -C "$STATE_DIR" diff --cached --quiet; then
        git -C "$STATE_DIR" commit -q -m "$1"
        git -C "$STATE_DIR" push -q origin crashbunny-state
    fi
}

# The build setup action edits tracked files (gradle.properties, signing info); keep copies to restore them
# after every reset to main, and keep them out of fix commits
backup_setup_files() {
    git -C "$REPO" diff --name-only > "$WORK/setup-files.txt"
    while read -r f; do mkdir -p "$WORK/setup-files/$(dirname "$f")"; cp "$REPO/$f" "$WORK/setup-files/$f"; done < "$WORK/setup-files.txt"
}

reset_repo() {
    git -C "$REPO" fetch -q origin main
    git -C "$REPO" checkout -q -f --detach origin/main
    git -C "$REPO" clean -q -fd
    while read -r f; do cp "$WORK/setup-files/$f" "$REPO/$f"; git -C "$REPO" update-index --assume-unchanged "$f"; done < "$WORK/setup-files.txt"
}

render() { # template output KEY=VALUE...
    local template="$1" target="$2"; shift 2
    python3 - "$template" "$target" "$@" <<'EOF'
import sys
text = open(sys.argv[1]).read()
for pair in sys.argv[3:]:
    key, value = pair.split("=", 1)
    text = text.replace("{{" + key + "}}", value)
open(sys.argv[2], "w").write(text)
EOF
}

run_claude() { # prompt-file log-file timeout-minutes
    local status=0
    (cd "$REPO" && timeout "${3}m" claude -p "$(cat "$1")" \
        --model "${CRASHBUNNY_MODEL:-opus}" \
        --output-format stream-json --verbose \
        --mcp-config "$WORK/mcp.json" \
        --allowedTools "Bash,Read,Edit,Write,Glob,Grep,mcp__firebase") > "$2" 2>&1 || status=$?
    echo "Claude exited with $status"
}

send_report() { # report-file
    {
        echo "## CrashBunny report"
        echo '```json'
        cat "$1"
        echo '```'
    } >> "$GITHUB_STEP_SUMMARY"
    if [ -z "${SIMPHONY_WEBHOOK_URL:-}" ]; then
        echo "SIMPHONY_WEBHOOK_URL is not set; report only written to the job summary"
        return
    fi
    curl -fsS -X POST "$SIMPHONY_WEBHOOK_URL" \
        -H "Authorization: Bearer $SIMPHONY_WEBHOOK_SECRET" \
        -H "Content-Type: application/json" \
        --data-binary "@$1"
}

is_final_run() {
    [ "$(date -u +%H)" = "$FINAL_HOUR_UTC" ] || [ "${FORCE_REPORT:-false}" = "true" ]
}

cat > "$WORK/mcp.json" <<EOF
{"mcpServers": {"firebase": {"command": "firebase", "args": ["mcp", "--only", "crashlytics"],
  "env": {"GOOGLE_APPLICATION_CREDENTIALS": "$GOOGLE_APPLICATION_CREDENTIALS"}}}}
EOF

setup_state
backup_setup_files
phase_out="$("${QUEUE[@]}" phase)"
phase="$(echo "$phase_out" | out phase)"
[ "${FORCE_TRIAGE:-false}" = "true" ] && phase="triage"
echo "Phase: $phase"

if [ "$phase" = "triage" ]; then
    night="$(echo "$phase_out" | out night_id)"
    start="$(echo "$phase_out" | out window_start)"
    end="$(echo "$phase_out" | out window_end)"
    [ -n "$night" ] || night="$(date -u -d '+6 hours' +%F)"
    [ -n "$start" ] || start="$(date -u -d '-24 hours' +%FT%TZ)"
    [ -n "$end" ] || end="$(date -u +%FT%TZ)"
    reset_repo
    git -C "$REPO" fetch -q --tags
    render "$HERE/triage-prompt.md" "$WORK/triage-prompt.md" \
        WINDOW_START="$start" WINDOW_END="$end" OUTPUT="$WORK/triage.json" STATE_FILE="$STATE_DIR/state.json"
    run_claude "$WORK/triage-prompt.md" "$WORK/logs/triage.jsonl" 120
    limit="$("${QUEUE[@]}" limit --log "$WORK/logs/triage.jsonl" | out limit)"
    if [ "$limit" != "none" ]; then
        echo "Hit the $limit usage limit during triage; the next hourly run retries"
        if is_final_run; then
            python3 -c 'import json,sys; json.dump({"run": {"window_start": sys.argv[1], "window_end": sys.argv[2], "dry_run": sys.argv[3] == "true", "workflow_run_url": sys.argv[4], "queue_status": "paused_weekly_limit" if sys.argv[5] == "weekly" else "in_progress", "pending": 0, "note": "Triage could not run because of the Claude usage limit"}, "tiers": [], "issues": []}, open(sys.argv[6], "w"))' \
                "$start" "$end" "$DRY_RUN" "$RUN_URL" "$limit" "$WORK/report.json"
            send_report "$WORK/report.json"
        fi
        exit 0
    fi
    if [ ! -s "$WORK/triage.json" ]; then
        echo "::error::Triage finished without writing triage.json"
        tail -n 50 "$WORK/logs/triage.jsonl"
        exit 1
    fi
    "${QUEUE[@]}" init --triage "$WORK/triage.json" --night-id "$night" \
        --window-start "$start" --window-end "$end" --dry-run "$DRY_RUN"
    save_state "Triage for $night"
    phase="fix"
fi

if [ "$phase" = "fix" ]; then
    while true; do
        item="$("${QUEUE[@]}" next --out "$WORK/item.json" --max-prs "$MAX_PRS" | out item)"
        [ -n "$item" ] || break
        save_state "Start $item"
        echo "::group::Fix $item"
        reset_repo
        rm -f "$WORK/result.json"
        render "$HERE/fix-prompt.md" "$WORK/fix-prompt.md" \
            ITEM_FILE="$WORK/item.json" RESULT_FILE="$WORK/result.json" PATCH_FILE="$WORK/patches/$item.patch"
        run_claude "$WORK/fix-prompt.md" "$WORK/logs/fix-$item.jsonl" 60
        limit="$("${QUEUE[@]}" limit --log "$WORK/logs/fix-$item.jsonl" | out limit)"
        "${QUEUE[@]}" complete --id "$item" --result "$WORK/result.json" --limit "$limit"
        save_state "Finish $item ($limit)"
        echo "::endgroup::"
        if [ "$limit" != "none" ]; then
            echo "Hit the $limit usage limit; the next hourly run continues the queue"
            break
        fi
    done
fi

# Report once the queue is drained (or on the last run of the night), then go idle until the next night
phase="$("${QUEUE[@]}" phase | out phase)"
if [ "$phase" = "report" ] || { [ "$phase" = "fix" ] && is_final_run; }; then
    "${QUEUE[@]}" report --run-url "$RUN_URL" --out "$WORK/report.json"
    send_report "$WORK/report.json"
    "${QUEUE[@]}" mark-reported
    save_state "Reported"
fi
