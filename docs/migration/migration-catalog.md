# Compose Migration Catalog and Process Guide

Single source of truth for **what** is migrated, **in which order**, **what must be prepared before each phase**, and **where every module
stands**. The design document (MS-1440) defines the strategy; this file turns it into a checklist the team and the migration agents follow.

Related documents:

- [viewmodel-udf.md](viewmodel-udf.md) — Phase 1 MVI/UDF contract.
- [compose-preparation.md](compose-preparation.md) — one-time Phase 2 foundation checklist.
- [compose-ui.md](compose-ui.md) and [compose-theme.md](compose-theme.md) — Phase 2 rules.
- [module-contracts.md](module-contracts.md) — orchestrator guardrails for every phase.
- [metrics/compose-migration-metrics.md](metrics/compose-migration-metrics.md) — metrics runbook.

Migration agents are Claude Code subagents in `.claude/agents/migration/`. Invoke them by name (for example "use the
`migration-p1-0-test-audit` agent for `:feature:consent`") or with an `@` mention. They return open questions to the main session
instead of guessing, and never commit. Run the `migration-setup` agent first on a new machine: it checks the locally installed skills
and tools the other agents need and prints install commands.

---

## 1 — Phases at a glance

| Phase                       | Scope                                                                        | Unit of work                  | Agents                                                                                                         | Ships to release                                      |
|-----------------------------|------------------------------------------------------------------------------|-------------------------------|----------------------------------------------------------------------------------------------------------------|-------------------------------------------------------|
| 0 — Foundation              | Docs, agents, metrics tooling, baseline                                      | Once                          | `migration-metrics`                                                                                            | n/a                                                   |
| 1 — Architecture            | ViewModels → MVI/UDF, Fragments state-driven, UI stays XML                   | Per module                    | `migration-p1-0-test-audit` → `migration-p1-1-planner` → `migration-p1-2-executor` → `migration-p1-3-reviewer` | Directly, **no feature flag**                         |
| 2 prep — Compose foundation | Common module, convention plugins, flag, lint, theme                         | Once                          | — (see `compose-preparation.md`)                                                                               | Release keeps flag `false`                            |
| 2 — Compose UI              | Compose screens next to legacy XML, in-module Navigation 3, adaptive layouts | Per module, one PR per screen | `migration-p2-1-planner` → `migration-p2-2-executor` → `migration-p2-3-reviewer`                               | No — behind `COMPOSE_UI_ENABLED` (debug/staging only) |
| 2 cleanup                   | Remove legacy UI and flag branches                                           | Per module, one PR            | `migration-p2-2-executor` (cleanup step) → `migration-p2-3-reviewer`                                           | Yes — this PR ships Compose                           |
| 3 — Orchestrator            | Orchestrator navigation to Compose/Navigation 3, remove Fragment shells      | Once, after all modules       | Not defined yet                                                                                                | Yes                                                   |

Phase order is strict: Phase 1 → Phase 2 → cleanup. Each stage can only start after all modules have gone past previous stage and have been
validated.

**Pilot exception:** `:feature:dashboard` (wave 0) goes through Phase 1 and Phase 2 on its own, before the other modules start
Phase 1, to validate the architecture, theme, documentation and agents. Although Phase 2 of the pilot might not be merged into main until
other modules are ready and serves only as a proof of architecture for future discussions and refinement.

For every other module, the rule above applies: Phase 2 starts only when every in-scope module is Phase 1 `done`,
and cleanup starts only when every in-scope module (Phase 2 not `n/a`) is Phase 2 `validated` or `done`.

---

## 2 — Module catalog

Inventory of modules with UI. Screen names are the current Fragment/Activity classes; counts come from `src/main` and must be re-checked by
`migration-p1-0-test-audit`/`migration-p2-1-planner` when the module is picked up.

| Module                              | Kind                        | Screens (current classes)                                                                                                                                                                                                                                                                                                     | Navigation                                                                 | Notes and risks                                                                                                                                                                                                       |
|-------------------------------------|-----------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `:feature:dashboard`                | Standalone hub (Activity)   | `DashboardActivity`; `MainFragment` with cards `ProjectDetailsFragment`, `DailyActivityFragment`, `SyncInfoFragment`; `SettingsFragment`, `AboutFragment` (`PreferenceFragmentCompat`); `FingerSelectionFragment`, `RequestLoginFragment`, `LogoutSyncFragment`, `LogoutSyncDeclineFragment`, `DebugFragment`, `BaseFragment` | `graph_dashboard.xml` (10 destinations), includes troubleshooting          | **Pilot.** Not an orchestrator step (`module-contracts.md` exempt). Portrait-locked. Preference screens have no Compose equivalent in AndroidX and need custom components. Inconsistent patterns from modularisation. |
| `:feature:troubleshooting`          | Standalone (from dashboard) | `TroubleshootingFragment`, `OverviewFragment`, `EventLogFragment`, `IntentLogFragment`, `NetworkingLogFragment`, `WorkerLogFragment`, `RealmToRoomRecordsMigrationFragment`                                                                                                                                                   | `graph_troubleshooting.xml`                                                | Extension of the dashboard pilot. Mostly lists; good proving ground for lazy lists.                                                                                                                                   |
| `:infra:ui-base`                    | Shared UI                   | `SettingsPasswordDialogFragment`                                                                                                                                                                                                                                                                                              | —                                                                          | Shared by dashboard and module-selector. Also hosts `applySystemBarInsets`.                                                                                                                                           |
| `:feature:alert`                    | Orchestrated step           | `AlertFragment`                                                                                                                                                                                                                                                                                                               | `graph_alert.xml` (included by many graphs)                                | Boundary destination for most modules; keep XML IDs.                                                                                                                                                                  |
| `:feature:exit-form`                | Orchestrated step           | `ExitFormFragment`                                                                                                                                                                                                                                                                                                            | `graph_exit_form.xml` (included by most graphs)                            | Boundary destination for most modules; form + IME handling.                                                                                                                                                           |
| `:feature:consent`                  | Orchestrated step           | `ConsentFragment`, `PrivacyNoticeFragment`                                                                                                                                                                                                                                                                                    | `graph_consent.xml`, `graph_privacy.xml`                                   | Two graphs; privacy notice is a separate entry.                                                                                                                                                                       |
| `:feature:setup`                    | Orchestrated step           | `SetupFragment`                                                                                                                                                                                                                                                                                                               | `graph_setup.xml`                                                          | Permissions and downloads; mostly progress UI.                                                                                                                                                                        |
| `:feature:select-subject-age-group` | Orchestrated step           | `SelectSubjectAgeGroupFragment`                                                                                                                                                                                                                                                                                               | `graph_age_group_selection.xml`                                            | List selection.                                                                                                                                                                                                       |
| `:feature:module-selector`          | Orchestrated step           | `ModuleSelectorFragment`                                                                                                                                                                                                                                                                                                      | `graph_module_selection.xml`                                               | Uses `SettingsPasswordDialogFragment`.                                                                                                                                                                                |
| `:feature:fetch-subject`            | Orchestrated step           | `FetchSubjectFragment`                                                                                                                                                                                                                                                                                                        | `graph_fetch_subject.xml`                                                  | Progress/error UI only.                                                                                                                                                                                               |
| `:feature:select-subject`           | Orchestrated step           | `SelectSubjectFragment`                                                                                                                                                                                                                                                                                                       | `graph_select_subject.xml`                                                 | No `@HiltViewModel` today; check where the logic lives.                                                                                                                                                               |
| `:feature:enrol-last-biometric`     | Orchestrated step           | `EnrolLastBiometricFragment`                                                                                                                                                                                                                                                                                                  | `graph_enrol_last.xml`                                                     | Progress/result UI.                                                                                                                                                                                                   |
| `:feature:validate-subject-pool`    | Orchestrated step           | `ValidateSubjectPoolFragment`                                                                                                                                                                                                                                                                                                 | `graph_validate_subject_pool.xml`                                          | Progress/result UI.                                                                                                                                                                                                   |
| `:feature:login`                    | Orchestrated step           | `LoginFormFragment`, `QrScannerFragment`                                                                                                                                                                                                                                                                                      | `graph_login.xml`                                                          | Unusual authentication flow; camera (QR) via interop.                                                                                                                                                                 |
| `:feature:matcher`                  | Orchestrated step           | `MatchFragment`                                                                                                                                                                                                                                                                                                               | `graph_matcher.xml`                                                        | Single screen, complex logic and progress states.                                                                                                                                                                     |
| `:feature:external-credential`      | Orchestrated step           | `ExternalCredentialControllerFragment` (entry), `ExternalCredentialScanQrFragment`, `ExternalCredentialScanOcrFragment`, `ExternalCredentialSearchFragment`, `ExternalCredentialSelectFragment`, `ExternalCredentialSkipFragment`                                                                                             | `graph_external_credential.xml` + `graph_external_credential_internal.xml` | Internal graph is the reference Navigation 3 candidate. Camera (QR/OCR).                                                                                                                                              |
| `:face:capture`                     | Orchestrated step           | `FaceCaptureControllerFragment` (entry), `PreparationFragment`, `LiveFeedbackFragment`, `ConfirmationFragment`                                                                                                                                                                                                                | `graph_face_capture.xml` + `graph_face_capture_internal.xml`               | Camera preview and custom Views; latency-sensitive.                                                                                                                                                                   |
| `:infra:camera`                     | Shared UI                   | Custom camera Views (no screens)                                                                                                                                                                                                                                                                                              | —                                                                          | Stays View-based behind `AndroidView` interop; migrated with `:face:capture`.                                                                                                                                         |
| `:fingerprint:connect`              | Orchestrated step           | `ConnectScannerControllerFragment` (entry), `ConnectFragment`, `BluetoothOffFragment`, `NfcOffFragment`, `NfcPairFragment`, `SerialEntryPairFragment`, `ScannerOffFragment`, `OtaFragment`, `OtaFailedFragment`, `OtaRecoveryFragment`                                                                                        | `graph_connect_scanner.xml` + `graph_connect_internal.xml`                 | Hardware flows; may be retired before its wave.                                                                                                                                                                       |
| `:fingerprint:capture`              | Orchestrated step           | `FingerprintCaptureFragment`, `FingerFragment`, `TryAnotherFingerSplashDialogFragment`                                                                                                                                                                                                                                        | `graph_fingerprint_capture.xml`                                            | Hardware flows; may be retired before its wave.                                                                                                                                                                       |
| `:feature:orchestrator`             | Host (Activity)             | `OrchestratorActivity`, `OrchestratorFragment`                                                                                                                                                                                                                                                                                | `graph_orchestration.xml`                                                  | Phase 1 only within the module waves; its navigation is replaced in Phase 3.                                                                                                                                          |

Out of scope: `:testing:data-generator` (developer tool), `:infra:test-tools` (test host Activity).

---

## 3 — Migration status

Machine-readable: `collect-compose-migration-metrics.py` parses the table between the markers.
Keep one row per module, the module path in backticks, and only the status values below.

**Phase 1 status:** `not-started` → `audit` → `planned` → `in-progress` → `done` (or `n/a`).
**Phase 2 status:** `not-started` → `planned` → `in-progress` → `validated` → `done` (or `n/a`).

| Transition            | Set by                                                                     |
|-----------------------|----------------------------------------------------------------------------|
| Phase 1 `audit`       | `migration-p1-0-test-audit`, when it writes the audit                      |
| Phase 1 `planned`     | `migration-p1-1-planner`, when it writes the handoff                       |
| Phase 1 `in-progress` | `migration-p1-2-executor`, when it starts implementing an approved handoff |
| Phase 1 `done`        | Human, after `migration-p1-3-reviewer` PASS and merge                      |
| Phase 2 `planned`     | `migration-p2-1-planner`, when it writes the handoff                       |
| Phase 2 `in-progress` | `migration-p2-2-executor`, in the first flagged screen PR                  |
| Phase 2 `validated`   | Human, when the handoff reaches `Cleanup validation status: APPROVED`      |
| Phase 2 `done`        | Human, after the cleanup PR passes `migration-p2-3-reviewer` and is merged |

Metrics mapping: Phase 2 `not-started`/`planned` = legacy UI, `in-progress`/`validated` = interop,
`done` = Compose. Rows with Phase 2 `n/a` are excluded from conversion percentages.

<!-- module-status:begin -->

| Module                              | Wave | Phase 1     | Phase 2     | Updated    | Notes                                 |
|-------------------------------------|------|-------------|-------------|------------|---------------------------------------|
| `:feature:dashboard`                | 0    | not-started | not-started | 2026-10-14 | Pilot                                 |
| `:feature:troubleshooting`          | 1    | not-started | not-started | 2026-10-14 | Extension of dashboard                |
| `:infra:ui-base`                    | 1    | n/a         | not-started | 2026-10-14 | Shared dialog only                    |
| `:feature:alert`                    | 2    | not-started | not-started | 2026-10-14 |                                       |
| `:feature:exit-form`                | 2    | not-started | not-started | 2026-10-14 |                                       |
| `:feature:consent`                  | 2    | not-started | not-started | 2026-10-14 |                                       |
| `:feature:setup`                    | 2    | not-started | not-started | 2026-10-14 |                                       |
| `:feature:select-subject-age-group` | 2    | not-started | not-started | 2026-10-14 |                                       |
| `:feature:module-selector`          | 2    | not-started | not-started | 2026-10-14 |                                       |
| `:feature:fetch-subject`            | 2    | not-started | not-started | 2026-10-14 |                                       |
| `:feature:select-subject`           | 2    | not-started | not-started | 2026-10-14 |                                       |
| `:feature:enrol-last-biometric`     | 2    | not-started | not-started | 2026-10-14 |                                       |
| `:feature:validate-subject-pool`    | 2    | not-started | not-started | 2026-10-14 |                                       |
| `:feature:login`                    | 3    | not-started | not-started | 2026-10-14 |                                       |
| `:feature:matcher`                  | 3    | not-started | not-started | 2026-10-14 |                                       |
| `:feature:external-credential`      | 3    | not-started | not-started | 2026-10-14 |                                       |
| `:face:capture`                     | 4    | not-started | not-started | 2026-10-14 |                                       |
| `:infra:camera`                     | 4    | n/a         | n/a         | 2026-10-14 | AndroidView interop with face capture |
| `:fingerprint:connect`              | 5    | not-started | not-started | 2026-10-14 | May be retired                        |
| `:fingerprint:capture`              | 5    | not-started | not-started | 2026-10-14 | May be retired                        |
| `:feature:orchestrator`             | 6    | not-started | n/a         | 2026-10-14 | Navigation replaced in Phase 3        |

<!-- module-status:end -->

---

## 4 — Metrics milestones

Run the metrics collection agent (`.claude/agents/migration/migration-metrics.md`) at these points, on a clean checkout of the merge commit,
with the label shown:

| When                                                    | Label              | Notes                                   |
|---------------------------------------------------------|--------------------|-----------------------------------------|
| Phase 0, before the first Phase 1 merge                 | `baseline`         | Reference row for every comparison      |
| After a module's Phase 1 merge                          | `p1:<module>`      | Startup and build time; UI is unchanged |
| Before the first Compose dependency merges              | `pre-compose`      | Isolates the cost of adding Compose     |
| Before a module's cleanup, flag on, `benchmark` variant | `p2-flag:<module>` | Compose vs legacy on the same commit    |
| After a module's cleanup merge                          | `p2:<module>`      | Shipped state                           |
| Ad hoc                                                  | `adhoc`            | Never used as a comparison base         |

Regressions beyond the thresholds in the metrics runbook block the next milestone for that module
until they are explained or fixed.
