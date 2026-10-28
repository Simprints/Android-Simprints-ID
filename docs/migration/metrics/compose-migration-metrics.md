# Compose migration metrics runbook

How migration metrics are measured, stored and compared. Collection is normally done by the metrics collection agent (
`.claude/agents/migration/migration-metrics.md`), which follows this runbook step by step.

Outputs:

- `docs/migration/metrics/results/history.ndjson` — **committed**, one JSON row per snapshot; the time series every comparison uses.
- `docs/migration/metrics/results/latest.json` — committed copy of the newest row.
- `docs/migration/metrics/results/fragments/*.json` — per-run inputs written by the `run-*.sh` scripts; git-ignored.

---

## 1. Rules that keep metrics comparable

1. **One commit per snapshot.** Every fragment is stamped with the commit it was measured on, and the collector rejects fragments from any
   other commit. Run all scripts on a clean checkout of the milestone commit; the scripts refuse to run on a dirty tree.
2. **Runtime and size are measured on the minified `benchmark` variant, never on debug**. Build time is measured on `assembleDebug`, which
   is what developers run.
3. **Same environment.** Startup on the same device model and OS image; build time on the same machine class. The fragments record the
   device and machine; `report-metrics.py` warns when they differ from the baseline or previous milestone.
4. **Stable scenarios.** Build scenarios target files that the migration does not rewrite (see `gradle-profiler.scenarios`). Changing a
   scenario or the benchmark test breaks comparability: collect a new `adhoc` row first and note the change in §6 below.
5. **Labels.** Only milestone labels (`baseline`, `pre-compose`, `p1:<module>`, `p2-flag:<module>`, `p2:<module>`) are used for comparisons.
   `adhoc` rows are for experiments and may use `--allow-dirty` / `--allow-stale`; milestone rows may not.
6. **Compose behind the flag.** Release keeps `COMPOSE_UI_ENABLED = false`. A `p2-flag:<module>` snapshot passes
   `-Psimprints.compose.<module-with-colons-as-dashes>=true` to the startup and size scripts and needs the library `benchmark` build type (
   `compose-preparation.md`).

---

## 2. Metrics and sources

| Metric                     | Key(s)                                                                                                | Source                                                                           | Script                                 |
|----------------------------|-------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------|----------------------------------------|
| Phase 1 progress           | `module.phase1_done_pct`                                                                              | Status table in `migration-catalog.md`                                           | `collect-compose-migration-metrics.py` |
| Module conversion          | `module.conversion_pct`, `module.{legacy,interop,compose}_modules`                                    | Same (Phase 2 `done` = Compose)                                                  | `collect-compose-migration-metrics.py` |
| View–Compose interop nodes | `interop.*_nodes`                                                                                     | `ComposeView(`/`simComposeView(`/`AndroidView(` call sites in production sources | `collect-compose-migration-metrics.py` |
| Code ignored in coverage   | `coverage.excluded_annotation_count`                                                                  | `@ExcludedFromGeneratedTestCoverageReports` usages in production sources         | `collect-compose-migration-metrics.py` |
| Line coverage (optional)   | `coverage.line_pct`                                                                                   | `jacocoTestReport.xml` files (`--with-jacoco`)                                   | `collect-compose-migration-metrics.py` |
| Build time                 | `build_time.<scenario>_seconds_{mean,median,min,max}`                                                 | gradle-profiler, `MEASURE` iterations only                                       | `run-gradle-profiler-metrics.sh`       |
| App size                   | `app_size.aab_bytes`, `app_size.download_bytes_reference_device`, `app_size.download_bytes_{min,max}` | `:id:bundleBenchmark` + `bundletool get-size total`                              | `run-app-size-metrics.sh`              |
| Cold startup               | `startup_time.timetoinitialdisplayms_{median,avg,min,max}`                                            | `:benchmark` macrobenchmark, 10 iterations                                       | `run-startup-benchmark-metrics.sh`     |
| Jank                       | —                                                                                                     | Not collected yet (`compose-preparation.md` CP-22)                               | —                                      |

---

## 3. One-time setup

1. Install `gradle-profiler` and `bundletool` (`brew install gradle-profiler bundletool`), Python 3.10+, and the Android SDK platform-tools.
2. Make sure `~/.android/debug.keystore` exists (bundletool signs the APK set with it).
3. Prepare the reference device for startup: same model and OS image for every run, charged, screen on, no other apps in the foreground.

---

## 4. Collecting a snapshot

Run from the repository root, on a clean checkout of the milestone commit:

```bash
# 1. Build time (~10–20 min, debug variant)
bash docs/migration/metrics/scripts/run-gradle-profiler-metrics.sh

# 2. App size (benchmark variant)
bash docs/migration/metrics/scripts/run-app-size-metrics.sh [gradle args]

# 3. Cold startup (benchmark variant, exactly one device connected)
bash docs/migration/metrics/scripts/run-startup-benchmark-metrics.sh [gradle args]

# 4. Optional: coverage
./gradlew jacocoTestReport

# 5. Merge everything into one row (run exactly once per snapshot)
python3 docs/migration/metrics/scripts/collect-compose-migration-metrics.py --label <label> [--with-jacoco]

# 6. Compare with baseline and previous milestone
python3 docs/migration/metrics/scripts/report-metrics.py
```

`[gradle args]` is empty except for `p2-flag:<module>` snapshots (`-Psimprints.compose.<module-with-colons-as-dashes>=true`).

The collector fails when a fragment is stale, the tree is dirty, the catalog has an invalid status, or the same label already exists for the
commit. Fix the cause instead of forcing.

Commit `history.ndjson` and `latest.json` together, in a PR titled `MS-1440 Metrics: <label>`, with the output of `report-metrics.py` in the
description.

---

## 5. Regression thresholds

`report-metrics.py` exits with status 1 when a metric grows beyond its threshold compared with the baseline or the previous milestone:

| Metric                            | Threshold                                                       |
|-----------------------------------|-----------------------------------------------------------------|
| Cold startup TTID/TTFD median     | +10%                                                            |
| Build time median (each scenario) | +15%                                                            |
| Download size, reference device   | +5% (an increase is expected once at `pre-compose`; explain it) |
| Coverage exclusions               | any increase vs. the previous milestone                         |

A flagged regression blocks the module's next milestone until it is explained in the metrics PR or fixed. If the report warns that the
device or machine differs, re-measure on the original environment before acting. The values live in `report-metrics.py`; keep this table in
sync when changing them.
