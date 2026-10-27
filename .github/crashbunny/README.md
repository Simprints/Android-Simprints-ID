# CrashBunny automation

The nightly workflow `.github/workflows/scheduled-crashbunny.yml` does the routine part of the Crash Bunny duty
(see the "Crash Bunny (Mobile)" Confluence page):

1. **Triage** (once per night): Claude Code reads Crashlytics through the Firebase MCP server. It goes through the
   tiers open crashes → open ANRs → open non-fatals → closed crashes → closed ANRs → closed non-fatals, and writes a
   verdict per issue (`triage-prompt.md`).
2. **Fix** (queue): for each `app_bug`/`regression` root cause, Claude Code writes a failing test, fixes the code,
   runs the module tests and opens a draft PR (`fix-prompt.md`).
3. **Report**: when the queue is empty, or on the 05:00 UTC run, the report goes to the Simphony job
   "Nightly CrashBunny report". That job files or updates Jira tickets under MS-268 and posts the summary to
   #clashbunny.

The workflow runs hourly from 22:00 to 05:00 UTC. All progress is committed to the `crashbunny-state` branch
(`queue.py`). When a run hits the Claude usage limit, the next hourly run continues. Unfinished items carry over to
the next night.

## Setup

All secrets and variables below go in the `CrashBunny` environment (Settings → Environments), restricted to the `main` branch.

| Kind | Name | Value |
| --- | --- | --- |
| Secret | `CLAUDE_CODE_OAUTH_TOKEN` | Output of `claude setup-token` (Enterprise seat, valid 1 year) |
| Secret | `CRASHBUNNY_FIREBASE_SA_JSON` | Service account key JSON with Crashlytics read access on `simprints-152315` |
| Secret | `CRASHBUNNY_GH_TOKEN` | Optional. A token that can push branches and open PRs, so the PR checks run on bot PRs |
| Secret | `SIMPHONY_CRASHBUNNY_WEBHOOK_URL` / `SIMPHONY_CRASHBUNNY_WEBHOOK_SECRET` | The Simphony job's webhook URL and secret |
| Variable | `CRASHBUNNY_DRY_RUN` | Anything except `false` keeps shadow mode: no pushes or PRs, and patches are uploaded as artifacts |
| Variable | `CRASHBUNNY_MODEL` | Optional, default `opus` |
| Variable | `CRASHBUNNY_MAX_PRS` | Optional, default `15`. A runaway guard, not a quota |

Use `workflow_dispatch` with `force-triage` and `force-report` to test outside the night window.
