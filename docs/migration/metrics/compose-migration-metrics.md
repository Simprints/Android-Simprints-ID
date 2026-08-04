# Compose migration metrics collection runbook

This document defines a repeatable process to collect all migration metrics into one place:

- `docs/migration/metrics/results/latest.json` (latest snapshot)
- `docs/migration/metrics/results/history.ndjson` (time-series history)

## 1. One-time setup

1. Fill `docs/migration/metrics/compose-module-status.csv` with one row per `:feature:*` module.
2. Install local tools:
    - `gradle-profiler` (for build time metrics)
    - `bundletool` (for download-size metrics)
3. Keep this baseline commit SHA (last XML-only baseline) in your notes. You will compare new snapshots against that baseline row in
   `history.ndjson`.

## 2. Metrics and where they come from

| Metric                         | Source                                                                     | Automated by                                                              |
|--------------------------------|----------------------------------------------------------------------------|---------------------------------------------------------------------------|
| Module Conversion %            | `docs/migration/metrics/compose-module-status.csv` + `settings.gradle.kts` | `collect-compose-migration-metrics.py`                                    |
| View-Compose Interop Nodes     | Kotlin source scan (`ComposeView`, `AndroidView`)                          | `collect-compose-migration-metrics.py`                                    |
| Code Ignored in Coverage       | `@ExcludedFromGeneratedTestCoverageReports` usage + optional JaCoCo XML    | `collect-compose-migration-metrics.py`                                    |
| Build Time (clean/incremental) | `gradle-profiler` benchmark CSV                                            | `run-gradle-profiler-metrics.sh` + `parse-gradle-profiler-csv.py`         |
| APK Download Size              | AAB artifact size                                                          | `record-metric-fragment.py`                                               |
| App Startup Time               | `:benchmark:connectedBenchmarkAndroidTest` benchmark JSON output           | `run-startup-benchmark-metrics.sh` + `parse-startup-benchmark-results.py` |

## 3. Step-by-step collection

### Step 1 - Collect static metrics

```bash
python3 docs/migration/metrics/scripts/collect-compose-migration-metrics.py
```

This creates/updates:

- `docs/migration/metrics/results/latest.json`
- `docs/migration/metrics/results/history.ndjson`

### Step 2 - Collect build time metrics (clean + incremental)

Run gradle-profiler:

```bash
bash docs/migration/metrics/scripts/run-gradle-profiler-metrics.sh
```

This writes a fragment:

- `docs/migration/metrics/results/fragments/build_time.json`

### Step 3 - Collect APK size metrics

Build the bundle:

```bash
./gradlew id:bundleDebug
```

Record bundle size (bytes) as a metric fragment:

```bash
python3 docs/migration/metrics/scripts/record-metric-fragment.py \
  docs/migration/metrics/results/fragments/apk_size.json \
  apk_size.aab_bytes=$(stat -f%z id/build/outputs/bundle/debug/id-debug.aab)
```

### Step 4 - Collect app startup metrics

Run startup benchmark tests from the benchmark module (requires a connected benchmark-capable device/emulator):

```bash
bash docs/migration/metrics/scripts/run-startup-benchmark-metrics.sh
```

This writes a fragment:

- `docs/migration/metrics/results/fragments/startup_time.json`

### Step 5 - Merge it into the unified snapshot:

```bash
python3 docs/migration/metrics/scripts/collect-compose-migration-metrics.py
```

## 4. Keeping metrics comparable

1. Always run against the same build variant (`debug` or dedicated `benchmark`) and keep it fixed.
2. Use the same device class/OS image for runtime performance.
3. Compare each new row against the baseline row from the XML-only commit.
4. Do not change module status semantics:
    - `legacy`: XML/View system
    - `interop`: mixed View/Compose
    - `compose`: fully Compose

## 5. Script quick reference

- `docs/migration/metrics/scripts/collect-compose-migration-metrics.py`  
  Collects static metrics and merges all metric fragments into one snapshot/history.

- `docs/migration/metrics/scripts/run-gradle-profiler-metrics.sh`  
  Runs gradle-profiler scenarios and emits `build_time.json`.

- `docs/migration/metrics/scripts/parse-gradle-profiler-csv.py`  
  Parses `benchmark.csv` and computes average scenario times in seconds.

- `docs/migration/metrics/scripts/run-startup-benchmark-metrics.sh`  
  Runs `:benchmark:connectedBenchmarkAndroidTest`, parses startup benchmark JSON output, and emits `startup_time.json`.

- `docs/migration/metrics/scripts/parse-startup-benchmark-results.py`  
  Parses benchmark output JSON files and computes startup metric aggregates.

- `docs/migration/metrics/scripts/record-metric-fragment.py`  
  Writes/updates a JSON fragment from `key=value` arguments for manual or scripted ingestion.
