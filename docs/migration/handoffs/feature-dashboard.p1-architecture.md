# Phase 1 architecture handoff — `:feature:dashboard`

Produced by `migration-p1-1-planner`. Input for `migration-p1-2-executor`, which may start only after this document is marked
`Manual review status: APPROVED`.

Path abbreviations used in sections 3 to 7 (section 8 lists every file with its full repository path):

- `P/` = `feature/dashboard/src/main/java/com/simprints/feature/dashboard/`
- `T/` = `feature/dashboard/src/test/java/com/simprints/feature/dashboard/`
- `R/` = `feature/dashboard/src/main/res/`

---

## 1. Target module

| Item                    | Value                                                                                                                                                                                                                                                                                                                                                                                                         |
|-------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Module                  | `:feature:dashboard` (Compose migration pilot, wave 0)                                                                                                                                                                                                                                                                                                                                                        |
| Scope                   | Every screen in the module: `DashboardActivity`, `BaseFragment`, `MainFragment` + cards `ProjectDetailsFragment`, `DailyActivityFragment`, `SyncInfoFragment` (3 hosts), `SettingsFragment`, `AboutFragment`, `FingerSelectionFragment`, `RequestLoginFragment`, `LogoutSyncFragment`, `LogoutSyncDeclineFragment`. `DebugFragment` is **removed from the application** (human decision 2026-11-24, step 11). |
| Out of scope            | The included graphs owned by other modules (`graph_login`, `graph_privacy`, `graph_troubleshooting`, `graph_module_selection`); only the navigation into them and the results coming back (`LoginResult`, `ModuleSelectorResult`) are in scope. No Compose, no Phase 2 planning. `SettingsPasswordDialogFragment` (`:infra:ui-base`) is used unchanged.                                                       |
| Approved shared change  | `BaseMviViewModel` (+ tests) in `:infra:ui-base`, package `com.simprints.infra.uibase.viewmodel` (step 1), implemented from `docs/migration/mvi-base-classes.md`. The only other file outside `:feature:dashboard` that changes is the dashboard row of `docs/migration/migration-catalog.md` §2 (step 11).                                                                                                   |
| Preparation date        | 2026-11-24                                                                                                                                                                                                                                                                                                                                                                                                    |
| Baseline commit         | `4aa6180b2a99a1c732060c34074b782d948cc449` (HEAD of `migration/ms-1440-pilot-phase-1`). `git diff b18e513451 HEAD -- feature/dashboard infra/ui-base infra/test-tools build-logic gradle` is empty, so the audit baseline `b18e513451` describes the same code.                                                                                                                                               |
| Test audit              | `docs/migration/handoffs/feature-dashboard.p1-testing.md`, `Manual review status: APPROVED` (2026-11-24), §11 reviewer notes applied                                                                                                                                                                                                                                                                          |
| Binding human decisions | `docs/migration/handoffs/feature-dashboard.p1-known-issues.md` (KI-1, KI-2)                                                                                                                                                                                                                                                                                                                                   |
| Contract guardrails     | `module-contracts.md` does not apply (dashboard is not an orchestrator step). Navigation IDs, nav args and cross-module result wiring are still frozen (section 6).                                                                                                                                                                                                                                           |

### 1.1 Files inspected

- **Production** (`feature/dashboard/src/main`): all 38 Kotlin files under `P/` (every file listed in section 3), `AndroidManifest.xml`,
  `R/navigation/graph_dashboard.xml`, `R/menu/app_menu.xml`, `R/xml/preference_general.xml`, `R/xml/preference_about.xml`,
  `R/layout/fragment_main.xml`, `fragment_logout_sync.xml`, `fragment_dashboard_card_project_details.xml`,
  `fragment_dashboard_card_daily_activity.xml`, `fragment_settings_password_input.xml` (default visibilities checked), `build.gradle.kts`.
- **Tests** (`feature/dashboard/src/test`): `MainFragmentTest`, `LogoutSyncFragmentTest`, `SyncInfoViewModelTest` (setup),
  `ObserveSyncInfoUseCaseTest` (rules), `tools/Espresso.kt`; the remaining classes through the audit inventory (§2) and an
  `InstantTaskExecutorRule` usage scan.
- **Out of module, for behaviour evidence**: `infra/ui-base/build.gradle.kts` and its source list,
  `infra/ui-base/.../navigation/NavigationResultExt.kt` (function list), `infra/test-tools/build.gradle.kts` (Turbine, MockK,
  coroutines-test
  are `api`), `infra/test-tools/.../TestCoroutineRule.kt`, `infra/test-tools/.../hilt/HiltExt.kt` (function list),
  `infra/core/.../livedata/LiveDataEvent.kt` and `LiveDataEventWithContent.kt` (`send` = `setValue` vs `postValue`),
  `infra/core/.../tools/activity/BaseActivity.kt` and `broadcasts/InternalBroadcaster.kt` (logout broadcast), `infra/core/.../CoreModule.kt`
  (`WorkManager` is provided), `feature/troubleshooting/.../AutoResettingClickCounter.kt`, `build-logic/convention/.../KotlinAndroid.kt`,
  `TestingUnitConventionPlugin.kt`, `ModuleFeatureConventionPlugin.kt`, `gradle/libs.versions.toml` (Kotlin 2.4.21, Turbine 1.2.1,
  fuzzywuzzy), repository-wide usage scans for every dead-code candidate.
- **Documents**: section 2.

---

## 2. Referenced documents

| Document                                                       | Section                     | Resulting constraint                                                                                                                                                                                                                                                                                                                                |
|----------------------------------------------------------------|-----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `docs/migration/viewmodel-udf.md`                              | §1                          | Actions are processed one at a time in order; effects are never lost while the UI is stopped. Both guarantees live in the base class, not in screens.                                                                                                                                                                                               |
| `docs/migration/viewmodel-udf.md`                              | §2                          | One `<Screen>MVI.kt` per screen with `internal` `UiState` (immutable data class with defaults), `UiAction`, `UiEffect`.                                                                                                                                                                                                                             |
| `docs/migration/viewmodel-udf.md`                              | §3                          | `BaseMviViewModel` lives in `:infra:ui-base`, `com.simprints.infra.uibase.viewmodel`; the pilot adds it with tests as an approved shared change. Effects only through `emitEffect` (buffered channel), never `MutableSharedFlow`. Long or open-ended work through `launchWork { }`. Results that must survive process death are state, not effects. |
| `docs/migration/viewmodel-udf.md`                              | §4                          | Reducers only; no mutable UI fields; `SavedStateHandle` values restored in `initialState`; `Simber` stays.                                                                                                                                                                                                                                          |
| `docs/migration/viewmodel-udf.md`                              | §5                          | Mapping table applied per LiveData member in section 4. Two recorded exceptions: KI-1 (navigation kept as state) and the "no value yet" rule (D-3).                                                                                                                                                                                                 |
| `docs/migration/viewmodel-udf.md`                              | §6                          | Fragments collect with `repeatOnLifecycle(STARTED)` on `viewLifecycleOwner`; navigation and results stay in the Fragment; user input goes through `onAction`. KI-1 uses a dedicated per-view collector (recorded deviation DV-1).                                                                                                                   |
| `docs/migration/viewmodel-udf.md`                              | §7                          | Multi-flow ViewModels (`SyncInfoViewModel`, `LogoutSyncViewModel`) expose one `StateFlow` built with `stateIn(viewModelScope, WhileSubscribed(5_000), initial)`.                                                                                                                                                                                    |
| `docs/migration/viewmodel-udf.md`                              | §8                          | No ViewModel is injected into another. `LogoutSyncViewModel` is split per screen rather than shared (section 4.9/4.10).                                                                                                                                                                                                                             |
| `docs/migration/viewmodel-udf.md`                              | §9                          | Tests use JUnit 4, MockK, Truth, Turbine, `TestCoroutineRule`, `runTest`; per-class `InstantTaskExecutorRule` removal; cover every action, rapid repeated input, effects emitted before collection, saved-state restoration.                                                                                                                        |
| `docs/migration/module-contracts.md`                           | Header note, Guardrails 5–7 | Dashboard is exempt from the contract guardrails, but the guardrail intent is applied to its boundary: nav graph IDs/actions unchanged, `handleResult` wiring unchanged, no `Activity.setResult`.                                                                                                                                                   |
| `docs/migration/handoffs/feature-dashboard.p1-testing.md`      | §2–§7, §10, §11             | Regression net and test proposals. Every `Covered` row is a must-not-regress item (section 5); every `Weak`/`Uncovered` row maps to a closing test and every proposal has a disposition (section 7). The §9 E2E gap is accepted for the pilot.                                                                                                      |
| `docs/migration/handoffs/feature-dashboard.p1-known-issues.md` | KI-1                        | Re-login `LoginParams` replay is kept: `UiState.loginRequest: LoginRequest(seq, params)?`, never cleared; per-view collector with `distinctUntilChangedBy { it.seq }` downstream of `flowWithLifecycle`; `navigate(...)` not `navigateSafely`; S-5 reworded. Deviation from §5/§6.                                                                  |
| `docs/migration/handoffs/feature-dashboard.p1-known-issues.md` | KI-2                        | Settings "last updated" summary is `UiEffect.ShowConfigLastUpdated(text)`, emitted from the initial load and after each refresh emission, after the config-updated effect; no state field, no reload on view re-creation. ST-9 final as written there.                                                                                              |
| `docs/migration/handoffs/feature-dashboard.p1-known-issues.md` | KI-3 … KI-7                 | Kept as-is in Phase 1 (human decision 2026-11-24), each pinned by the tests named there: KI-3 language relaunch timing (DV-3), KI-4 SyncInfo restart side effects (DV-6), KI-5 RequestLogin dialog re-show (unconfirmed, PC-7), KI-6 config failure skips the root check (M-5, PC-13), KI-7 unknown finger crashes Finger selection (FS-4, PC-73).  |
| `docs/migration/mvi-base-classes.md`                           | All                         | Source of truth for `BaseMviViewModel` (API, action queue, `launchWork`, effect channel, `CancellationException` guard, backing-field fallback, tests B-1…B-9) and the `stubMvi` Fragment-test helper. D-1 and step 1 implement it; this handoff does not restate it.                                                                               |
| `.github/copilot-instructions.md`                              | §1, §5, §10, §11            | `internal` visibility, `by viewBinding`, `Simber`, Hilt `@HiltViewModel` + `@Inject constructor`, `@DispatcherIO`, test conventions. Where it prescribes `LiveData`/`LiveDataEvent`, `viewmodel-udf.md` wins for this module.                                                                                                                       |
| `.github/event-system.md`                                      | Adding Events               | The module logs no session events. The only event write is `DeviceEventTracker.trackDeviceConfigurationUpdatedEvent` inside the device-configuration transform (Settings); its position relative to the state change is preserved.                                                                                                                  |

---

## 3. Current-state inventory

### 3.1 ViewModels

| ViewModel                                                | Vis.     | LiveData / event members (current)                                                                                                                                                                     | Collaborators                                                                                                                                                       | Hosts                                                                                    |
|----------------------------------------------------------|----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------|
| `P/main/MainViewModel.kt`                                | internal | `consentRequired: LiveData<Boolean>`; `rootedDeviceDetected: LiveData<LiveDataEvent>` observed with plain `observe` (sticky)                                                                           | `ConfigRepository`, `SecurityManager`                                                                                                                               | `MainFragment`                                                                           |
| `P/main/projectdetails/ProjectDetailsViewModel.kt`       | internal | `projectCardStateLiveData: LiveData<DashboardProjectState>`; public `load()` called from `init` and `onResume`                                                                                         | `ConfigRepository`, `RecentUserActivityManager`, `TokenizationProcessor`                                                                                            | `ProjectDetailsFragment`                                                                 |
| `P/main/dailyactivity/DailyActivityViewModel.kt`         | internal | `dailyActivity: LiveData<DashboardDailyActivityState>`; public `load()` (init + `onResume`); `getCurrentDateAsString()` called at render time                                                          | `RecentUserActivityManager`, `TimeHelper`                                                                                                                           | `DailyActivityFragment`                                                                  |
| `P/settings/syncinfo/SyncInfoViewModel.kt`               | internal | `var isPreLogoutUpSync`; `loginNavigationEventLiveData` (plain `MutableLiveData<LoginParams>`, KI-1); lazy `syncInfoLiveData` (`asLiveData`, 5 s timeout); internal `MutableSharedFlow` click triggers | `ConfigRepository`, `AuthStore`, `RecentUserActivityManager`, `TimeHelper`, `ObserveSyncInfoUseCase`, `SyncOrchestrator`, `DeviceStateDataTracker`, `@DispatcherIO` | `SyncInfoFragment` ×3 hosts (separate instances)                                         |
| `P/settings/SettingsViewModel.kt`                        | internal | `generalConfiguration`, `faceConfiguration`, `languagePreference`, `settingsLocked`, `sinceConfigLastUpdated` (`LiveDataEventWithContent`, `postValue`), `configUpdated` (`LiveDataEvent`, `setValue`) | `ConfigRepository`, `SyncOrchestrator`, `ConfigSyncCache`, `DeviceEventTracker`                                                                                     | `SettingsFragment`                                                                       |
| `P/settings/about/AboutViewModel.kt`                     | internal | `syncAndSearchConfig`, `modalities`, `recentUserActivity`, `settingsLocked`, `logoutDestinationEvent` (`LiveDataEventWithContent<LogoutDestination>`), `openTroubleshooting` (`LiveDataEvent`)         | `ConfigRepository`, `DeviceStateDataTracker`, `SyncOrchestrator`, `RecentUserActivityManager`; `AutoResettingClickCounter`                                          | `AboutFragment`                                                                          |
| `P/settings/fingerselection/FingerSelectionViewModel.kt` | internal | `fingerSelections: LiveData<List<FingerSelectionSection>>`; `start()` from every `onViewCreated`; public models with `var` fields                                                                      | `ConfigRepository`                                                                                                                                                  | `FingerSelectionFragment`                                                                |
| `P/logout/LogoutSyncViewModel.kt`                        | internal | `logoutEventLiveData` (no production consumer); `isLogoutWithoutSyncVisibleLiveData` (1 s debounce); `settingsLocked` getter creating a new `liveData {}` per access; `logout()`                       | `ConfigRepository`, `SyncOrchestrator`, `AuthStore`, `DeviceStateDataTracker`                                                                                       | `LogoutSyncFragment`, `LogoutSyncDeclineFragment` (separate instances, disjoint members) |

No ViewModel uses `SavedStateHandle`. No ViewModel injects another ViewModel.

### 3.2 Activity and Fragments

| Class                                                   | Vis.     | Current UI logic that the refactor touches                                                                                                                                                                                                                                |
|---------------------------------------------------------|----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `P/DashboardActivity.kt`                                | public   | `onLogout(isProjectEnded)` → `navigate(action_to_requestLoginFragment, RequestLoginFragmentArgs(logoutReason))`, swallows `IllegalArgumentException` (`:23-40`). No ViewModel. **Unchanged in Phase 1.**                                                                  |
| `P/base/BaseFragment.kt`                                | internal | `onResume` auth redirect (`:16-23`). No ViewModel. **Unchanged in Phase 1.**                                                                                                                                                                                              |
| `P/requestlogin/RequestLoginFragment.kt`                | internal | Logout-reason dialog once via `onSaveInstanceState` flag (`:48`, `:52`, `:61-64`), 10-tap counter (`:54-58`), auth redirect on resume (`:66-71`). No ViewModel. **Unchanged in Phase 1** (decision D-9).                                                                  |
| `P/main/MainFragment.kt`                                | internal | Debug menu removal (`:31-33`), two observers (`:34-41`), menu navigation (`:44-51`)                                                                                                                                                                                       |
| `P/main/projectdetails/ProjectDetailsFragment.kt`       | internal | Render (`:24-42`), `onResume → load()` (`:45-48`)                                                                                                                                                                                                                         |
| `P/main/dailyactivity/DailyActivityFragment.kt`         | internal | `onResume → load()` (`:28-31`), visibility/divider/plural rules and a ViewModel call during render (`:33-112`)                                                                                                                                                            |
| `P/settings/syncinfo/SyncInfoFragment.kt`               | internal | Sets VM `var` in `onCreateView` (`:43-53`), two `handleResult` bindings with host-dependent back-stack entry (`:67-80`, `:362-366`), click wiring (`:90-128`), ~200 lines of host-config + state render logic (`:151-358`), login navigation observer (`:146-148`)        |
| `P/settings/SettingsFragment.kt`                        | internal | Six observers (`:62-90`), password gate decision from `settingsLocked.value` (`:145-159`), language dialog built from VM `.value`s (`:161-175`), language change = VM call + summary + relaunch (`:192-202`)                                                              |
| `P/settings/about/AboutFragment.kt`                     | internal | Five observers (`:84-113`), password/confirmation decision (`:148-158`, `:178-192`), password result routing (`:115-125`)                                                                                                                                                 |
| `P/settings/fingerselection/FingerSelectionFragment.kt` | internal | `start()` per view (`:32`), adapter pulls `viewModel.fingerSelections.value` (`:39-41`)                                                                                                                                                                                   |
| `P/logout/sync/LogoutSyncFragment.kt`                   | public   | `observe` registered inside `repeatOnLifecycle(RESUMED)` (adds an observer on every resume, `:47-56`)                                                                                                                                                                     |
| `P/logout/syncdecline/LogoutSyncDeclineFragment.kt`     | public   | Observes a new LiveData on every confirm click and decides password vs confirmation (`:56-72`)                                                                                                                                                                            |
| `P/debug/DebugFragment.kt`                              | internal | Five injected collaborators, `asLiveData().observe` (`:67-88`), `runBlocking` on main (`:107-120`), `cleanAll` on `lifecycleScope` + IO (`:122-132`), private sync-state mapping (`:151-157`). No ViewModel. **Removed from the application in Phase 1** (D-11, step 11). |

### 3.3 Domain logic, models, adapters

| File                                                                                                    | Role                                                                                            | Phase 1                                                           |
|---------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|-------------------------------------------------------------------|
| `P/settings/syncinfo/usecase/ObserveSyncInfoUseCase.kt`                                                 | Combines connectivity, auth, sync state, device data, config into `SyncInfo`                    | Unchanged (tests S-15 added)                                      |
| `P/settings/syncinfo/usecase/ObserveConfigurationChangesUseCase.kt`                                     | Config/module-count stream                                                                      | Unchanged                                                         |
| `P/settings/syncinfo/usecase/internal/GetSyncInfoSection{Records,Images}UseCase.kt`                     | Pure section mappers                                                                            | Unchanged (S-14 added)                                            |
| `P/settings/syncinfo/SyncInfo.kt`                                                                       | `SyncInfo` model tree + dead `LogoutActionReason` (`:91-94`)                                    | `LogoutActionReason` deleted in step 10                           |
| `P/settings/syncinfo/SyncInfoFragmentConfig.kt`, `P/view/ConfigurableSyncInfoFragmentContainer.kt`      | Host configuration read from container attributes (public)                                      | Unchanged                                                         |
| `P/settings/syncinfo/modulecount/*`                                                                     | `ModuleCount`, `ModuleCountAdapter`, `ModuleCountViewHolder`                                    | Unchanged                                                         |
| `P/settings/fingerselection/FingerSelectionItemAdapter.kt`                                              | Adapter with `getItems` lambda; `TemplateIdentifier.toString(context)` (public)                 | Adapter takes a submitted immutable list (step 2)                 |
| `P/main/projectdetails/DashboardProjectState.kt`, `P/main/dailyactivity/DashboardDailyActivityState.kt` | Current state classes                                                                           | Replaced by `UiState` types and deleted (steps 3, 4)              |
| `P/settings/about/LogoutDestination.kt`, `SyncAndSearchConfig.kt`                                       | Event payload enum; summary model                                                               | `LogoutDestination` deleted (step 9); `SyncAndSearchConfig` kept |
| `P/requestlogin/LogoutReason.kt`                                                                        | `@Keep @Parcelize` nav argument                                                                 | Unchanged (frozen, section 6)                                     |
| `P/tools/Fuzzy.kt`                                                                                      | Public `fuzzySearch`, no caller anywhere in the repository; only user of `libs.fuzzywuzzy.core` | Deleted with the module dependency line (step 12)                 |
| `R/layout/fragment_settings_password_input.xml`                                                         | No reference anywhere                                                                           | Deleted (step 12)                                                 |

### 3.4 Tests

The audit inventory (§2: 22 classes, 200 tests, all green at the baseline) is the starting point. `InstantTaskExecutorRule` is used by
`MainViewModelTest`, `ProjectDetailsViewModelTest`, `DailyActivityViewModelTest`, `SyncInfoViewModelTest`, `ObserveSyncInfoUseCaseTest`
(not needed even today: the class touches no LiveData), `SettingsViewModelTest`, `AboutViewModelTest`, `FingerSelectionViewModelTest`,
`LogoutSyncViewModelTest`. Support files: `T/tools/Espresso.kt` (both helpers unused),
`T/tools/di/Fake{Core,Events,Login,Security}Module.kt`.

### 3.5 Architecture problems found

| #     | Problem                                                                                                                                                                                                          | Where                                                                                                     |
|-------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| AP-1  | A mutable VM field is written by the Fragment in `onCreateView` and read lazily by the state stream and the auto-logout collector; correctness depends on call order (audit R-2).                                | `SyncInfoFragment.kt:51`, `SyncInfoViewModel.kt:52`, `:72`, `:84`, `:152`, `:201`, `:216`                 |
| AP-2  | The state stream reads its own LiveData value while being built (`syncInfoLiveData.value` inside `syncInfoLiveData`).                                                                                            | `SyncInfoViewModel.kt:117`, `:127`                                                                        |
| AP-3  | Every restart of the shared upstream (after ≥ 5 s without an active observer) launches another never-ending `syncImagesAfterEventsWhenRequired` collector and re-runs the initial-sync decision.                 | `SyncInfoViewModel.kt:85-87`, `:214-227`                                                                  |
| AP-4  | ~200 lines of decision logic combining host config and state are Fragment-resident and untested (audit R-3).                                                                                                     | `SyncInfoFragment.kt:151-358`                                                                             |
| AP-5  | Navigation request modelled as a never-cleared `MutableLiveData` (KI-1).                                                                                                                                         | `SyncInfoViewModel.kt:54-56`, `SyncInfoFragment.kt:146-148`                                               |
| AP-6  | Fragments read `LiveData.value` to make decisions (password gate, dialog contents, adapter data).                                                                                                                | `SettingsFragment.kt:149`, `:162-170`; `AboutFragment.kt:150`, `:182`; `FingerSelectionFragment.kt:39-41` |
| AP-7  | A new LiveData (and observer) is created per access / per click; an observer is added on every resume.                                                                                                           | `LogoutSyncViewModel.kt:46-49`, `LogoutSyncDeclineFragment.kt:57`, `LogoutSyncFragment.kt:48-55`          |
| AP-8  | One ViewModel class backs two screens that use disjoint members; one member (`logoutEventLiveData`) has no production consumer.                                                                                  | `LogoutSyncViewModel.kt`                                                                                  |
| AP-9  | Logic with side effects lives in a Fragment without a ViewModel; `runBlocking` on the main thread; IO work on `lifecycleScope`.                                                                                  | `DebugFragment.kt:67-132`                                                                                 |
| AP-10 | Mixed event semantics: a `LiveDataEvent` observed with plain `observe` is sticky (rooted overlay), others are consumed; `send` is `setValue` for `LiveDataEvent` but `postValue` for `LiveDataEventWithContent`. | `MainFragment.kt:39-41`; `infra/core/.../livedata/*`                                                      |
| AP-11 | Language change relaunches the app in parallel with the persist coroutine (audit R-5).                                                                                                                           | `SettingsFragment.kt:192-202`, `SettingsViewModel.kt:58-73`                                               |
| AP-12 | Mutable public model fields (`var finger`, `var quantity`) mutated during aggregation.                                                                                                                           | `FingerSelectionViewModel.kt:34-48`, `:56-59`                                                             |
| AP-13 | Catch-all `Throwable` (also swallows `CancellationException`) in the project card load.                                                                                                                          | `ProjectDetailsViewModel.kt:50`                                                                           |
| AP-14 | Dead code: `logoutEventLiveData`, `LogoutActionReason`, `fuzzySearch` (+ dependency), `fragment_settings_password_input.xml`, `typeSearchViewText`/`clickCloseChipIcon`.                                         | section 3.3, `T/tools/Espresso.kt`                                                                        |

AP-3 (KI-4), AP-11 (KI-3) and AP-13 are **kept as-is** for parity (recorded in section 4.0 and the known-issues document); AP-1, AP-2,
AP-4 to AP-8, AP-10, AP-12 and AP-14 are resolved by the refactor; AP-9 disappears with the removal of `DebugFragment` (D-11). AP-1 is
resolved without changing behaviour (decision D-7).

---

## 4. Per-screen MVI contract

### 4.0 Cross-cutting decisions and recorded deviations

| ID   | Decision                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     | Source                                                       |
|------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------|
| D-1  | **Base class.** `BaseMviViewModel` exactly as specified in `docs/migration/mvi-base-classes.md` (the source of truth: public API, action queue, effect channel, `launchWork`, the `CancellationException` guard and its rationale, the backing-field fallback, tests B-1…B-9). This handoff does not restate it. Pilot-specific use: `SyncInfoViewModel` and `LogoutSyncViewModel` override `uiState` (spec §3.1); `init { onAction(Load) }` blocks go after the properties `handleAction` uses (spec §3.2). | `mvi-base-classes.md`; `viewmodel-udf.md` §1, §3             |
| D-2  | **Effects** always go through `emitEffect`. Internal trigger flows that are not effects (`SyncInfoViewModel` click triggers with `extraBufferCapacity = 1`) stay `MutableSharedFlow`; they are private plumbing of the state stream.                                                                                                                                                                                                                                                                         | `viewmodel-udf.md` §3                                        |
| D-3  | **"No value yet" rule.** A LiveData that had no initial value maps to a nullable `UiState` field defaulting to `null`; while `null` the Fragment leaves the XML default untouched. This keeps today's pre-load appearance (privacy menu item visible, project card visible and empty, audio-alert preference visible, logout-without-sync button visible and info text invisible).                                                                                                                           | `viewmodel-udf.md` §5; parity                                |
| D-4  | **Static navigation stays in the Fragment.** Clicks that need no ViewModel input (toolbar up, menu items, preference → sub-screen, select-modules, settings button, external settings intents, info popups, device-ID copy) call `navigateSafely`/`navigate`/`startActivity` directly, exactly as today. They are not ViewModel entry points, so `onAction` is not involved. Everything that reads ViewModel data or decides goes through `onAction`.                                                        | `viewmodel-udf.md` §6 ("Navigation … stays in the Fragment") |
| D-5  | **Collection.** `repeatOnLifecycle(STARTED)` on `viewLifecycleOwner` for state and effects. Exceptions: KI-1 collector (DV-1). `LogoutSyncFragment` moves from `RESUMED` to `STARTED` (DV-5).                                                                                                                                                                                                                                                                                                                | `viewmodel-udf.md` §6                                        |
| D-6  | **Process death.** No screen needs `SavedStateHandle`: no value is persisted today, every effect is user-initiated and equivalent to today's `LiveDataEvent` loss, KI-1 says the login request is safe to lose, and `RequestLoginFragment` keeps its `onSaveInstanceState` flag unchanged. Unlocked-settings state stays ViewModel-lifetime (parity).                                                                                                                                                        | `viewmodel-udf.md` §3, §4; known-issues KI-1                 |
| D-7  | **SyncInfo pre-logout mode (AP-1, R-2).** The Fragment sends `HostAttached(isPreLogoutUpSync)` from `onCreateView` (same place as today). The ViewModel stores it once in reducer state (`isPreLogoutUpSync: Boolean? = null`, first value wins) and **gates** the data stream, the initial-sync decision, the image auto-start and the auto-logout collector on the first non-null value. Order no longer matters: nothing reads the mode before it is known.                                               | audit R-2, S-3                                               |
| D-8  | **SyncInfo render rules (AP-4, R-3).** The host config (`SyncInfoFragmentConfig`) stays a Fragment value resolved in `onCreateView` (static per Fragment instance, also needed for result routing). Every decision that combines config and state, or chooses between alternatives, moves to pure functions in `P/settings/syncinfo/SyncInfoRenderRules.kt`, unit-tested without Robolectric. The Fragment only applies their results and formats strings.                                                   | `viewmodel-udf.md` §6; audit R-3                             |
| D-9  | **No ViewModel for `DashboardActivity`, `BaseFragment`, `RequestLoginFragment`.** They hold no LiveData and no ViewModel; their logic is the logout terminal step and a one-line auth redirect. Moving `RequestLoginFragment`'s dialog-once flag into a ViewModel would change when the dialog re-appears (a Fragment field reset in `onViewCreated` vs. ViewModel-lifetime state), so Phase 1 only pins them with P0 tests. Revisit at Phase 2 planning.                                                    | Phase 1 objectives (ViewModel/LiveData scope); parity        |
| D-10 | **`LogoutSyncViewModel` split.** The two screens use disjoint members, and §3 requires exactly one effect collector per ViewModel. `LogoutSyncDeclineViewModel` (new) takes password + logout; `LogoutSyncViewModel` keeps visibility and moves next to its screen.                                                                                                                                                                                                                                          | `viewmodel-udf.md` §3, §8; AP-8                              |
| D-11 | **`DebugFragment` is removed from the application** (human decision 2026-11-24): the Fragment, its layout, nav destination and action, the debug menu item and its `BuildConfig.DEBUG` wiring, and its test. No `DebugViewModel`. Removing the nav destination is an **approved exception** to "nav IDs unchanged": `:feature:dashboard` is exempt from `module-contracts.md`, nothing outside the module references `debugFragment`, and the human ordered it.                                              | Human decision 2026-11-24                                    |
| D-12 | **Fragment tests stub the MVI streams explicitly** with the shared helper from `mvi-base-classes.md` §6 (`T/tools/MviTestExt.kt`, step 1): a `MutableStateFlow` for `uiState` and a `Channel` for `uiEffect`. Relaxed mocks of `StateFlow` do not emit, and `StateFlow.collect` returns `Nothing`, so an unstubbed relaxed mock is never acceptable (audit structural note).                                                                                                                                 | audit §2 structural note, R-7                                |
| D-13 | **Visibility.** All new types are `internal`. Existing public types (`DashboardActivity`, `LogoutSyncFragment`, `LogoutSyncDeclineFragment`, `SyncInfoFragmentConfig`, `ConfigurableSyncInfoFragmentContainer`, `FingerSelectionSection`, `FingerSelectionItem`, `TemplateIdentifier.toString`, `LogoutReason`) keep their visibility. `FingerSelectionItem` fields change from `var` to `val` (module-internal users only).                                                                                 | copilot-instructions §1                                      |

**Recorded deviations** (the reviewer must accept each one):

| ID   | Deviation                                                                                                                                                                                                                                                                                                                                                                                               | From                                                  | Why                                                                                                     | Removed by                                         |
|------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------|---------------------------------------------------------------------------------------------------------|----------------------------------------------------|
| DV-1 | **KI-1.** Re-login request is state (`loginRequest`, never cleared, `seq` per press), consumed by a dedicated per-view collector (`flowWithLifecycle(STARTED)` then `distinctUntilChangedBy { it.seq }`), navigating with `navigate(graph_login, params.toBundle())`. Comment `// Parity with legacy LiveData replay, see feature-dashboard.p1-known-issues.md KI-1` in the ViewModel and the Fragment. | `viewmodel-udf.md` §5, §6                             | Human decision: keep today's replay to every new view, no replay on stop/start, navigate on every press | Post-Phase-1 KI-1 fix (`UiEffect.NavigateToLogin`) |
| DV-2 | **KI-2.** "Last updated" summary stays a one-time `UiEffect.ShowConfigLastUpdated(text)` (lost after view re-creation, as today).                                                                                                                                                                                                                                                                       | (none: §5 standard mapping)                           | Human decision; listed so nobody "fixes" it by moving it to state                                       | Post-Phase-1 KI-2 fix                              |
| DV-3 | **KI-3.** Settings language selection keeps today's Fragment sequence: `onAction(LanguageSelected)`, immediate summary write from the selected code, then relaunch (`finishAffinity` + start `DashboardActivity`) without waiting for the persist (AP-11, R-5).                                                                                                                                         | `viewmodel-udf.md` §6 ("Fragment makes no decisions") | Human decision: keep the restart behaviour (2026-11-24)                                                 | Post-Phase-1 KI-3 fix                              |
| DV-4 | `BaseMviViewModel` adds the `CancellationException` guard that the `viewmodel-udf.md` §3 sample does not show. **Accepted** (human decision 2026-11-24); specified in `mvi-base-classes.md` §3.3.                                                                                                                                                                                                       | `viewmodel-udf.md` §3 sample                          | Parity with one coroutine per call; otherwise one collaborator can stop a screen's whole queue          | n/a (the §3 sample stays as is)                    |
| DV-5 | `LogoutSyncFragment` collects in `STARTED` instead of observing inside `RESUMED`.                                                                                                                                                                                                                                                                                                                       | (parity)                                              | §6 rule; removes the observer-per-resume leak; the window between start and resume is not user-visible  | n/a                                                |
| DV-6 | **KI-4.** `SyncInfoViewModel` keeps AP-3 (each upstream restart re-runs the initial-sync decision and starts another image auto-start collector in pre-logout mode).                                                                                                                                                                                                                                    | (parity)                                              | Human decision: keep in Phase 1 (2026-11-24)                                                            | Post-Phase-1 KI-4 fix                              |

**`launchWork { }` inventory** (`viewmodel-udf.md` §3, everything else runs inside `handleAction`):

| ViewModel           | Work in `launchWork`                                                                                                                   | Why                                                                            |
|---------------------|----------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| `SettingsViewModel` | `syncOrchestrator.refreshConfiguration().collect { … }` after `UpdateConfigClicked`                                                    | Open-ended; must not delay `LanguageSelected` (R-5: the activity is finishing) |
| `SyncInfoViewModel` | Auto-logout collector (started in `init`); initial-sync decision and pre-logout image auto-start (started from the upstream `onStart`) | Never-ending observation; today separate `viewModelScope.launch`es             |

### 4.1 FingerSelection (`FingerSelectionFragment`, `FingerSelectionViewModel`)

```kotlin
internal data class FingerSelectionUiState(val sections: List<FingerSelectionSection>? = null) // null: not loaded (D-3)
internal sealed interface FingerSelectionUiAction {
    data object Load : FingerSelectionUiAction
}
internal sealed interface FingerSelectionUiEffect // none in Phase 1
```

| Current                                    | Target                                                                                                                                                   |
|--------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| `fingerSelections: LiveData<List<…>>`      | `uiState.sections`                                                                                                                                       |
| `start()` from every `onViewCreated`       | `onAction(Load)` from every `onViewCreated` (parity: config re-read per view, list replaced, never appended)                                             |
| Adapter `getItems = { viewModel.….value }` | Adapter holds a submitted immutable `List<FingerSelectionSection>`; the Fragment submits on each non-null `sections` and calls `notifyDataSetChanged`    |
| Mutable aggregation (`quantity++`)         | Immutable aggregation with first-seen order (for example `groupingBy { it }.eachCount()` on the ordered list); `FingerSelectionItem` fields become `val` |

Process death: nothing persisted (re-loaded). `launchWork`: none.

**KI-7 (kept):** the aggregation keeps every configured entry, including `TemplateIdentifier.NONE`, and `toString(context)` keeps
throwing for it, so an unknown finger type still crashes the list. Pinned by FS-4 (step 2).

### 4.2 ProjectDetails card (`ProjectDetailsFragment`, `ProjectDetailsViewModel`)

```kotlin
internal data class ProjectDetailsUiState(
    val isLoaded: Boolean? = null, // null until the first load finishes: card keeps its XML default (visible, empty)
    val title: String = "",
    val lastUser: String = "",
    val lastScanner: String = "",
)
internal sealed interface ProjectDetailsUiAction {
    data object Load : ProjectDetailsUiAction
} // from init and every onResume
internal sealed interface ProjectDetailsUiEffect // none
```

- `init { onAction(Load) }` and `ProjectDetailsFragment.onResume → onAction(Load)` (R-10: two loads at creation, one per resume, as today).
- Load body unchanged, including `Raw` pass-through, `Tokenized` decrypt with `TokenKeyType.AttendantId` and the cached project, `null`
  project → `isLoaded = false`, catch-all `Throwable` → `isLoaded = false` (AP-13 kept).
- Render: card visible iff `isLoaded == true` (no-op while `null`), title, `dashboard_project_card_current_user` format, scanner row hidden
  iff `lastScanner` empty else `dashboard_project_card_scanner_used` format.
- `DashboardProjectState.kt` deleted (replaced by `ProjectDetailsUiState`).

### 4.3 DailyActivity card (`DailyActivityFragment`, `DailyActivityViewModel`)

```kotlin
internal data class DailyActivityUiState(val activity: DailyActivityUi? = null) { // null: no load finished (card is gone in XML)
    data class DailyActivityUi(
        val enrolments: Int, val identifications: Int, val verifications: Int,
        val currentDate: String, // captured at load time; today read at render time, and every resume triggers a load
    ) {
        val hasNoActivity: Boolean;
        val isEnrolmentsDividerVisible: Boolean;
        val isIdentificationsDividerVisible: Boolean
    }
}
internal sealed interface DailyActivityUiAction {
    data object Load : DailyActivityUiAction
} // init + every onResume
internal sealed interface DailyActivityUiEffect // none
```

- The divider and "has X" rules move from `DashboardDailyActivityState` and the Fragment into `DailyActivityUi` (pure, unit-tested); the
  Fragment renders every view on every state (card, three groups, two dividers, plural labels) so visible → gone re-renders work (D-1).
- `getCurrentDateAsString()` is removed from the public API (the Fragment no longer calls the ViewModel during render).
- `DashboardDailyActivityState.kt` deleted.

### 4.4 Main (`MainFragment`, `MainViewModel`)

```kotlin
internal data class MainUiState(
    val isConsentRequired: Boolean? = null,  // null: privacy item keeps XML default (visible)
    val isRootedDeviceDetected: Boolean = false, // sticky state, never an effect (audit R-4 a, M-2)
)
internal sealed interface MainUiAction {
    data object Load : MainUiAction
} // init only
internal sealed interface MainUiEffect // none
```

- `Load` keeps the order: consent read first, then the root check; a failing configuration load still skips the root check and the
  exception still escapes (**KI-6**, pinned by M-5; the base-class guard catches only cancellation).
  `Simber.e("Rooted device detected", ex)`
  unchanged.
- Menu navigation stays in the Fragment (D-4). The debug menu item and its `!BuildConfig.DEBUG` removal stay unchanged in step 5 and are
  deleted in step 11 (D-11).

### 4.5 LogoutSyncDecline (`LogoutSyncDeclineFragment`, new `LogoutSyncDeclineViewModel`)

```kotlin
internal data object LogoutSyncDeclineUiState // the screen renders nothing from the ViewModel
internal sealed interface LogoutSyncDeclineUiAction {
    data object ConfirmClicked : LogoutSyncDeclineUiAction  // reads the password config freshly on every click (LV-8)
    data object LogoutConfirmed : LogoutSyncDeclineUiAction // confirmation dialog positive button or password success
}
internal sealed interface LogoutSyncDeclineUiEffect {
    data class ShowPasswordDialog(val password: String) : LogoutSyncDeclineUiEffect
    data object ShowLogoutConfirmation : LogoutSyncDeclineUiEffect
}
```

- `LogoutConfirmed` → `syncOrchestrator.execute(OneTime.Logout.start())` (exact command, LV-1). Navigation after logout stays with the
  `DashboardActivity` broadcast.
- Cancel button and toolbar → `popBackStack()` in the Fragment (D-4). `SettingsPasswordDialogFragment.registerForResult` stays in the
  Fragment and forwards success as `LogoutConfirmed`. The dialog title stays `dashboard_password_lock_title_logout`.

### 4.6 LogoutSync (`LogoutSyncFragment`, `LogoutSyncViewModel` moved to `P/logout/sync/`)

```kotlin
internal data class LogoutSyncUiState(val isLogoutWithoutSyncVisible: Boolean? = null) // null: XML defaults (button visible, info invisible)
internal sealed interface LogoutSyncUiAction  // none: the only click is static navigation (D-4)
internal sealed interface LogoutSyncUiEffect  // none
```

- `override val uiState` = `combine(observeSyncState(), observeDeviceDataState()) { !eventsCompleted || imagesSyncing || hasDataToUpload }
  .debounce(1_000).map { LogoutSyncUiState(it) }.stateIn(viewModelScope, WhileSubscribed(5_000), LogoutSyncUiState())` (§7; the 5 s
  matches `asLiveData`'s default timeout).
- Render: `logout_sync_info` and `logoutWithoutSyncButton` `VISIBLE`/`INVISIBLE` (not `GONE`) when non-null.
- Deleted: `logoutEventLiveData`, the `AuthStore` dependency, `settingsLocked` and `logout()` (moved in step 6).

### 4.7 Settings (`SettingsFragment`, `SettingsViewModel`)

```kotlin
internal data class SettingsUiState(
    val isFingerprintSettingsVisible: Boolean? = null, // finger selection + audio alert; null: XML defaults
    val isFaceAutoCaptureVisible: Boolean? = null,
    val languageCode: String? = null,                  // summary rendered from it after the persist
    val languageOptions: List<String> = emptyList(),
    val passwordConfig: SettingsPasswordConfig = SettingsPasswordConfig.NotSet,
)
internal sealed interface SettingsUiAction {
    data object LanguageClicked : SettingsUiAction
    data class PasswordUnlocked(val action: SettingsGatedAction) : SettingsUiAction // SettingsGatedAction.LANGUAGE <-> "language"
    data class LanguageSelected(val code: String) : SettingsUiAction
    data object UpdateConfigClicked : SettingsUiAction
}
internal sealed interface SettingsUiEffect {
    data class ShowPasswordDialog(val password: String, val action: SettingsGatedAction) : SettingsUiEffect
    data class ShowLanguageDialog(val options: List<String>, val selectedCode: String?) : SettingsUiEffect
    data object ShowConfigUpdateScheduled : SettingsUiEffect
    data object ConfigUpdated : SettingsUiEffect                      // toast
    data class ShowConfigLastUpdated(val text: String) : SettingsUiEffect // KI-2 / DV-2
}
```

| Current                                                          | Target                                                                                                                                                                                                                                                                        |
|------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `load()` in `init`                                               | `init` performs the load: emit `ShowConfigLastUpdated(configSyncCache.sinceLastUpdateTime())` first (sent before the Fragment collects; relies on the buffered channel), then one `reduce` with language, fingerprint/face visibility, language options, password config      |
| `generalConfiguration` / `faceConfiguration` observers           | `isFingerprintSettingsVisible` (`modalities.contains(FINGERPRINT)`), `isFaceAutoCaptureVisible` (`face?.isAutoCapture == true`)                                                                                                                                               |
| Fragment `showPasswordIfRequired` reading `settingsLocked.value` | `LanguageClicked` → `ShowPasswordDialog(password, LANGUAGE)` if `passwordConfig.getNullablePassword() != null`, else `ShowLanguageDialog(languageOptions, languageCode)`                                                                                                      |
| Password success → `unlockSettings()` + show dialog              | `PasswordUnlocked(LANGUAGE)` → `reduce { passwordConfig = Unlocked }`, then `ShowLanguageDialog`                                                                                                                                                                              |
| `updateLanguagePreference(code)`                                 | `LanguageSelected(code)` in `handleAction`: `updateDeviceConfiguration { apply language; trackDeviceConfigurationUpdatedEvent(it, isLocalChange = true) }`, then `reduce { languageCode = code }`, then `Simber.i("Language set to $code", tag = SETTINGS)` (order unchanged) |
| Fragment `handleLanguageChange`                                  | Unchanged sequence (DV-3): `onAction(LanguageSelected)`, write the summary for the selected code, relaunch                                                                                                                                                                    |
| `scheduleConfigUpdate()` + Fragment "scheduled" summary          | `UpdateConfigClicked` → `emitEffect(ShowConfigUpdateScheduled)`, then `launchWork { refreshConfiguration().collect { emitEffect(ConfigUpdated); emitEffect(ShowConfigLastUpdated(…)) } }` (toast before summary, KI-2)                                                        |
| `configUpdated` / `sinceConfigLastUpdated` observers             | Effects; the Fragment shows the toast / sets the update-config summary only from effects; no summary in state, no reload on view re-creation (KI-2)                                                                                                                           |

Static navigation (finger selection, sync info, about) and toolbar up stay in the Fragment (D-4). The code → name mapping
(`R.array.language_values`/`language_array`) stays in the Fragment because it reads resources.

### 4.8 About (`AboutFragment`, `AboutViewModel`)

```kotlin
internal data class AboutUiState(
    val syncAndSearchConfig: SyncAndSearchConfig? = null,
    val isScannerVersionVisible: Boolean? = null, // modalities.contains(FINGERPRINT); null: XML default (hidden)
    val lastScannerVersion: String? = null,
    val passwordConfig: SettingsPasswordConfig = SettingsPasswordConfig.NotSet,
)
internal sealed interface AboutUiAction {
    data object LogoutClicked : AboutUiAction
    data object LogoutConfirmed : AboutUiAction        // confirmation dialog positive button
    data object TroubleshootingClicked : AboutUiAction // sync & search preference click
    data class PasswordUnlocked(val action: AboutGatedAction) : AboutUiAction // LOGOUT <-> "logout", TROUBLESHOOTING <-> "troubleshooting"
}
internal sealed interface AboutUiEffect {
    data class ShowPasswordDialog(val password: String, val action: AboutGatedAction) : AboutUiEffect
    data object ShowLogoutConfirmation : AboutUiEffect
    data object NavigateToLogoutSync : AboutUiEffect
    data object NavigateToTroubleshooting : AboutUiEffect
}
```

- `LogoutClicked`: password set → `ShowPasswordDialog(LOGOUT)`, else `ShowLogoutConfirmation`.
- `LogoutConfirmed` and `PasswordUnlocked(LOGOUT)` (the latter after `reduce { Unlocked }`) run the unchanged decision:
  `canSyncDataToSimprints() && (hasEventsToUpload() || hasSamplesToUpload())` → `NavigateToLogoutSync`; otherwise
  `execute(OneTime.Logout.start())` and **no effect** (today's `LoginScreen` event is consumed without action; navigation comes from the
  `DashboardActivity` broadcast). `hasEventsToUpload` keeps its try/catch (`Throwable` → `true` + `Simber.i(…, tag = SYNC)`,
  `CancellationException` rethrown; the base-class guard, `mvi-base-classes.md` §3.3, keeps the queue alive afterwards).
  `LogoutDestination.kt` is deleted.
- `TroubleshootingClicked`: `AutoResettingClickCounter.handleClick(viewModelScope)`; on threshold → password set ?
  `ShowPasswordDialog(TROUBLESHOOTING)` : `NavigateToTroubleshooting`. `PasswordUnlocked(TROUBLESHOOTING)` → unlock, then
  `NavigateToTroubleshooting`.
- The Fragment keeps version, device ID, clipboard + toast, summary formatting (`lowerCaseCapitalized`) and toolbar up (D-4).

### 4.9 SyncInfo [shared: main card, standalone `syncInfoFragment`, panel in `LogoutSyncFragment`]

```kotlin
internal data class SyncInfoUiState(
    val isPreLogoutUpSync: Boolean? = null, // set once by HostAttached (D-7)
    val syncInfo: SyncInfo = SyncInfo(),    // initial value = today's first render with SyncInfo()
    val loginRequest: LoginRequest? = null, // KI-1: never cleared
)
internal data class LoginRequest(val seq: Int, val params: LoginParams)
internal sealed interface SyncInfoUiAction {
    data class HostAttached(val isPreLogoutUpSync: Boolean) : SyncInfoUiAction // from onCreateView
    data object SyncRecordsClicked : SyncInfoUiAction                          // forceEventSync(canEmitSyncButtonClick = true)
    data object SyncImagesClicked : SyncInfoUiAction                           // toggleImageSync
    data object RequestLogin : SyncInfoUiAction                                // KI-1 name
    data class LoginResultReceived(val result: LoginResult) : SyncInfoUiAction
    data class ModuleSelectionResultReceived(val result: ModuleSelectorResult) : SyncInfoUiAction // isConfirmed check moves into the VM
}
internal sealed interface SyncInfoUiEffect // empty in Phase 1 (KI-1 keeps login navigation as state); NavigateToLogin after the KI-1 fix
```

**State stream** (`viewmodel-udf.md` §7, keeping today's timing):

1. `preLogoutMode` = first non-null `isPreLogoutUpSync` from the reducer state (`super.uiState`).
2. `dataLayerDriven` = `preLogoutMode.flatMapLatest { observeSyncInfo(it) }` with the existing `onStart` hooks (each now `launchWork`, each
   awaiting `preLogoutMode`) and `shareIn(viewModelScope, WhileSubscribed(), replay = 1)` (unchanged).
3. Forced-progress branches unchanged, reading the current value from the inner state (fixes AP-2 without changing behaviour).
4. `syncInfoState` = `merge(…).distinctUntilChanged().flowOn(ioDispatcher).stateIn(viewModelScope, WhileSubscribed(), SyncInfo())` —
   an internal hop with no stop timeout, like today's `shareIn`.
5. `override val uiState` = `combine(super.uiState, syncInfoState) { local, info -> local.copy(syncInfo = info) }
   .stateIn(viewModelScope, WhileSubscribed(5_000), SyncInfoUiState())`. Total stop delay stays 5 s (today: `asLiveData` 5 s +
   `WhileSubscribed()` 0 s), so the initial-sync decision is re-run on the same returns as today (DV-6), and the last `SyncInfo` is
   re-delivered without a default-state flash (both `stateIn` keep their replay value).

**Actions:** `HostAttached` stores the mode if still `null`. `SyncRecordsClicked` = today's `forceEventSync()` body
(`deviceStateDataTracker.refresh()` first, optimistic trigger only if events are not syncing, then
`OneTime.Events.restart(!mode && project RUNNING)`). `SyncImagesClicked` = today's toggle. `RequestLogin` builds `LoginParams` exactly as
today and sets `LoginRequest(seq = previous + 1, params)`. `LoginResultReceived` → `forceEventSync()` on success.
`ModuleSelectionResultReceived` → `forceEventSync(canEmitSyncButtonClick = false)` iff `isConfirmed`. `performLogout()` becomes private
(only the auto-logout uses it), exact command `OneTime.Logout.start()`.

**Fragment:** sends `HostAttached(config.isSyncInfoLogoutOnComplete)` in `onCreateView`; keeps `syncInfoConfig` for rendering and for
`getCurrentDestinationId()` (moved into `SyncInfoRenderRules`); `handleResult` bindings stay in `onViewCreated` with the same
destination IDs and forward results as actions; state collected in `STARTED`; KI-1 collector as in the known-issues document; clicks
per D-4 or `onAction`. `SyncInfoRenderRules.kt` (pure) holds: `isSyncButtonVisible`, `isFooterSyncInProgressVisible`,
`isModuleSectionVisible`, `isImageSectionVisible`, `syncButtonText` (`@StringRes`), `imageSyncButtonText`/`imageSyncButtonColor`,
`errorPopupMessage(SyncInfoError)` (sealed: too many requests > maintenance with outage > maintenance > generic), `progressLine(part)`
(pending / done / ongoing without counters / ongoing with counters), `currentDestinationId(config)`.

Deleted: `loginNavigationEventLiveData`, `syncInfoLiveData`, `var isPreLogoutUpSync`, public `forceEventSync`/`toggleImageSync`/
`requestNavigationToLogin`/`handleLoginResult`, `LogoutActionReason`.

### 4.10 Debug — removed (human decision 2026-11-24)

No contract. `DebugFragment` and everything that exists only for it are deleted in step 11 (D-11): the Fragment, `fragment_debug.xml`,
the `debugFragment` destination and `action_mainFragment_to_debugFragment`, the `debug` menu item, `MainFragment`'s
`!BuildConfig.DEBUG` removal block and `R.id.debug` branch, and the `MainFragmentTest` debug-navigation test. The screen had no
string, drawable or dimension resources (hard-coded labels) and no Hilt module of its own: every binding it injected (`SyncOrchestrator`,
`ResetDownSyncInfoUseCase`, `AuthStore`, `EventRepository`, `EnrolmentRecordRepository`, `@DispatcherIO`) is
provided by another module and still used elsewhere, so no DI code changes.

### 4.11 Unchanged screens

`DashboardActivity`, `BaseFragment`, `RequestLoginFragment`: no production change (D-9). Their behaviour is pinned by A-1…A-3 and
RL-1…RL-4 before any screen that ends in a logout is refactored. `RequestLoginFragment` keeps **KI-5** (dialog may re-show after
returning from troubleshooting, unconfirmed; PC-7).

---

## 5. Behavior parity checklist

Every row is a must-not-regress item. "Audit" names the audit row; "Pinned by" names the test that must assert it after the screen's step.
`(C)` = rated `Covered` in the audit, `(W)` = `Weak`, `(U)` = `Uncovered`, `(N)` = not in the audit.

### 5.1 Entry, logout terminal step, request login

| ID   | Behaviour                                                                                                                                     | Audit        | Pinned by                                                                                            |
|------|-----------------------------------------------------------------------------------------------------------------------------------------------|--------------|------------------------------------------------------------------------------------------------------|
| PC-1 | Signed in on resume → `mainFragment`; signed out → `requestLoginFragment`                                                                     | 3.1, 4 (C)   | `BaseFragmentTest` (unchanged)                                                                       |
| PC-2 | Logout broadcast → `requestLoginFragment`, `LogoutReason` (project-ending strings) iff `isProjectEnded`, else `null`                          | 5.1, 6.1 (U) | A-1, A-2                                                                                             |
| PC-3 | Second broadcast while on request login: no crash, destination unchanged                                                                      | 6.1 (U)      | A-3                                                                                                  |
| PC-4 | Device ID and version texts; signed in on resume → main; signed out → stays                                                                   | 3.9, 4 (C)   | `RequestLoginFragmentTest` (unchanged)                                                               |
| PC-5 | `LogoutReason` arg → dialog title/body/close; shown once across `recreate()` (saved instance flag); no reason → no dialog                     | 6.9 (U)      | RL-1, RL-2, RL-4                                                                                     |
| PC-6 | 10 quick logo taps → troubleshooting; 9 → stays                                                                                               | 6.9 (U)      | RL-3                                                                                                 |
| PC-7 | **KI-5 (unconfirmed):** logout-reason dialog may re-show after returning from troubleshooting; Phase 1 does not change `RequestLoginFragment` | (N)          | No unit test (view not re-created by `TestNavHostController`); confirm on a device, audit §9 flow 18 |

### 5.2 Main and cards

| ID    | Behaviour                                                                                                                               | Audit               | Pinned by                                                      |
|-------|-----------------------------------------------------------------------------------------------------------------------------------------|---------------------|----------------------------------------------------------------|
| PC-10 | Consent required ↔ `collectConsent` (true and false); privacy item visible / hidden accordingly                                         | 3.2, 4 (C)          | `MainViewModelTest` (rewritten), M-4, `MainFragmentTest`       |
| PC-11 | Rooted (`RootedDeviceException`) → overlay visible, logged with `Simber.e`; non-rooted → no overlay                                     | 3.2 (C), 6.2 (U)    | `MainViewModelTest`, M-1, M-3, M-4                             |
| PC-12 | Overlay is **sticky**: still visible after view re-creation                                                                             | 6.2 (U), R-4 a      | M-2                                                            |
| PC-13 | **KI-6:** root check runs after the consent load; a failing config read skips it and the exception escapes (crash)                      | 6.2 (U)             | M-5 (pins current behaviour, step 5 before/after)              |
| PC-14 | Menu → settings / privacy (`graph_privacy`); menu → debug until step 11, after which the item and destination no longer exist (D-11)    | 3.2 (C)             | `MainFragmentTest` (debug test deleted in step 11 by decision) |
| PC-15 | Project card: loaded state with exact `decrypt(AttendantId, project)`; `Raw` passes through; null project or any throwable → not loaded | 3.3, 4 (C), 6.3 (U) | `ProjectDetailsViewModelTest` (rewritten), P-5, P-6            |
| PC-16 | Project card render: card visible iff loaded, title, formatted user, scanner row visible iff non-empty with formatted text              | 3.3 (W), 4 (W)      | P-1, P-2, P-3                                                  |
| PC-17 | Cards reload on every resume as well as on creation (R-10)                                                                              | 6.3, 6.4 (U)        | P-4, D-2                                                       |
| PC-18 | Daily activity state = today's counts; date from `TimeHelper`                                                                           | 3.4 (C)             | `DailyActivityViewModelTest` (rewritten)                       |
| PC-19 | Daily card shown iff any count > 0; title contains date; each group shown with count iff > 0; divider rules                             | 3.4 (C), 4 (W)      | `DailyActivityFragmentTest` (re-pointed), D-1                  |
| PC-20 | Visible → gone re-render for card, groups and dividers                                                                                  | 6.4 (U)             | D-1                                                            |
| PC-21 | Plural labels via `getQuantityString`                                                                                                   | 6.4 (U)             | D-3                                                            |

### 5.3 SyncInfo [shared by three hosts]

| ID    | Behaviour                                                                                                                                                                                                                                                                                                                                     | Audit                   | Pinned by                                             |
|-------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------|-------------------------------------------------------|
| PC-30 | Sync now → `deviceStateDataTracker.refresh()` then `Events.restart(isDownSyncAllowed = !preLogout && RUNNING)` (6 cases incl. paused/ending/logged out); stop-then-start                                                                                                                                                                      | 3.5 (C)                 | `SyncInfoViewModelTest` (rewritten)                   |
| PC-31 | Image toggle: start when idle, stop when syncing                                                                                                                                                                                                                                                                                              | 3.5 (C)                 | `SyncInfoViewModelTest`                               |
| PC-32 | Optimistic progress for events/images, hand-over to data-driven progress; none when events already syncing / when stopping images                                                                                                                                                                                                             | 3.5, 4 (C)              | `SyncInfoViewModelTest`                               |
| PC-33 | Module-selection confirmed → restart **without** optimistic progress; not confirmed → nothing                                                                                                                                                                                                                                                 | 6.5 (U)                 | S-4, S-6                                              |
| PC-34 | Initial sync: no history / > 5 min / pre-logout regardless / pre-logout with modules required → restart; recently synced / running / modules required (normal) → none; uninitialised state skipped; exactly 5 min → none                                                                                                                      | 3.5, 4 (C), 6.5 (U)     | `SyncInfoViewModelTest`, S-16                         |
| PC-35 | Initial-sync decision re-runs when the stream restarts after ≥ 5 s without a collector (DV-6)                                                                                                                                                                                                                                                 | (N)                     | N-3                                                   |
| PC-36 | Pre-logout: images start once per events-completed transition; not in normal mode                                                                                                                                                                                                                                                             | 6.5 (U), R-1            | S-1                                                   |
| PC-37 | Pre-logout auto-logout exactly `OneTime.Logout.start()` 3 s after ready; none when not pre-logout, records syncing, images syncing, data pending, images restart after pause                                                                                                                                                                  | 3.5 (W), 4 (C)          | S-2, `SyncInfoViewModelTest` negatives                |
| PC-38 | Sign-out does not make the ViewModel issue commands                                                                                                                                                                                                                                                                                           | 4 (C)                   | `SyncInfoViewModelTest`                               |
| PC-39 | Pre-logout mode applied before anything reads it (logout host: `observeSyncInfo(true)` only, down-sync disabled, auto-logout armed)                                                                                                                                                                                                           | 5.5, 6.5 (U), R-2       | S-3, N-1, S-7                                         |
| PC-40 | **KI-1**: re-login `LoginParams(projectId = signedInProjectId, userId = signedInUserId ?: recent user)`; one press navigates once; stop/start of the same view does not navigate again; re-creating the view navigates again with the same params; a second press navigates again; navigation uses `navigate(graph_login, params.toBundle())` | 3.5 (C/W), 5.1 (U), 6.5 | S-5 (KI-1 wording), S-12 + KI-1 Fragment parity cases |
| PC-41 | Login result success → event sync restart; failure → nothing                                                                                                                                                                                                                                                                                  | 3.5, 4 (C)              | `SyncInfoViewModelTest`, S-6 (wiring)                 |
| PC-42 | Result routing per host: back-stack entry `logOutSyncFragment` / `syncInfoFragment` / `mainFragment`                                                                                                                                                                                                                                          | 5.2 (U)                 | S-6, `SyncInfoRenderRulesTest`                        |
| PC-43 | Host-config rendering (toolbar, status header, settings button, section headers/footer, combo counter, image section gating, module section and list, sync-button visibility, in-progress footer only on logout host)                                                                                                                         | 6.5 (U), R-3            | S-7, S-8, S-9, S-13, `SyncInfoRenderRulesTest`        |
| PC-44 | Button texts, image button colour, progress text composition and bar percentage                                                                                                                                                                                                                                                               | 6.5 (U)                 | S-8, S-11, S-13, `SyncInfoRenderRulesTest`            |
| PC-45 | Error popup priority: too many requests > maintenance with outage > maintenance > generic                                                                                                                                                                                                                                                     | 6.5 (U)                 | S-10, `SyncInfoRenderRulesTest`, S-14                 |
| PC-46 | Clicks: module selection (both triggers) → `graph_module_selection`; settings button → `syncInfoFragment`; CommCare → app details intent; offline → wireless settings; toolbar up                                                                                                                                                             | 5.1 (U)                 | S-9, S-12                                             |
| PC-47 | Before the first emission the default `SyncInfo()` is rendered                                                                                                                                                                                                                                                                                | 6.5 (U)                 | S-13                                                  |
| PC-48 | Use cases unchanged: `ObserveSyncInfoUseCase` (17), `ObserveConfigurationChangesUseCase` (5), section use cases (45 + 11)                                                                                                                                                                                                                     | 3.5, 4 (C)              | Existing tests unchanged + S-14, S-15                 |

### 5.4 Settings

| ID    | Behaviour                                                                                                                                                                                    | Audit                 | Pinned by                                 |
|-------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------|-------------------------------------------|
| PC-50 | Initial load: language, fingerprint/face visibility, password config, "since last updated"                                                                                                   | 3.6 (C)               | `SettingsViewModelTest` (rewritten)       |
| PC-51 | Language change persists `language`, tracks `DeviceConfigurationUpdatedEvent(isLocalChange = true)` inside the transform, **then** updates the language output, then logs                    | 3.6 (C), 5.3, R-5     | `SettingsViewModelTest`, ST-6             |
| PC-52 | Language flow: password gate, single-choice dialog of configured codes with names, current selected, persist + summary + relaunch to `DashboardActivity`                                     | 6.6 (U)               | ST-3, ST-4, N-8                           |
| PC-53 | `LanguageSelected` is not delayed by an in-flight configuration refresh                                                                                                                      | (N), R-5              | N-4                                       |
| PC-54 | Unlock persists for later actions (ViewModel lifetime)                                                                                                                                       | 3.6 (C), 6.6 (U)      | `SettingsViewModelTest`, ST-7             |
| PC-55 | Update config: "scheduled" summary, refresh collected, toast then "last updated" per emission; no emission → neither                                                                         | 3.6 (C), 6.6 (U)      | ST-5, ST-10, `SettingsViewModelTest`      |
| PC-56 | **KI-2**: "last updated" shown on first render, XML default after view re-creation, shown again after the next update; emitted before the Fragment collects and still delivered              | 6.6, R-4 c            | ST-9 (final, known-issues), N-5           |
| PC-57 | Toolbar shown; finger-selection visible iff fingerprint (and hidden after a fingerprint → non-fingerprint re-render); audio alert follows fingerprint; face auto-capture iff `isAutoCapture` | 3.6, 4 (C/W), 6.6 (U) | `SettingsFragmentTest`, ST-1, ST-2, ST-11 |
| PC-58 | Language summary shows the language name                                                                                                                                                     | 3.6 (C)               | `SettingsFragmentTest`                    |
| PC-59 | Sync info / finger selection / about navigation; toolbar up                                                                                                                                  | 3.6 (C), 6.6 (U)      | `SettingsFragmentTest`, ST-8              |

### 5.5 About

| ID    | Behaviour                                                                                                                                                                  | Audit               | Pinned by                                   |
|-------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------|---------------------------------------------|
| PC-60 | Initial load: sync & search config (CommCare fallback), modalities, scanner version, password config                                                                       | 3.7 (C), 6.7 (U)    | `AboutViewModelTest` (rewritten), AB-7      |
| PC-61 | Logout decision: can up-sync and (events or samples pending) → logout-sync screen, else exact `Logout.start()` and no navigation; fresh pending read per request           | 3.7, 4 (C/W)        | `AboutViewModelTest`, AB-1, AB-3            |
| PC-62 | `hasPendingEvents()` failure → treated as pending (no logout); `CancellationException` → nothing, later actions still processed                                            | 6.7 (U), R-9        | AB-2, N-6                                   |
| PC-63 | Logout without password → confirmation → logout; cancel → nothing; with password → correct → unlock + logout once; wrong → nothing                                         | 3.7, 4 (C)          | `AboutFragmentTest` (re-pointed), AB-5, N-7 |
| PC-64 | Troubleshooting: 5 clicks, each within 1 s of the previous (`AutoResettingClickCounter` defaults) → (password gate) → `graph_troubleshooting`                              | 3.7 (C), 6.7 (U)    | `AboutViewModelTest`, AB-4, N-7             |
| PC-65 | Version, device ID, sync & search summary; scanner version visible with fingerprint, hidden otherwise (incl. re-render); toolbar shown; device ID copy + toast; toolbar up | 3.7, 4 (C), 6.7 (U) | `AboutFragmentTest`, AB-6, AB-8, AB-9       |

### 5.6 FingerSelection

| ID    | Behaviour                                                                                                                                                   | Audit            | Pinned by                                  |
|-------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------|--------------------------------------------|
| PC-70 | Sections "SimMatcher" then "NEC"; duplicates aggregated by count in first-seen order                                                                        | 3.8 (C)          | `FingerSelectionViewModelTest` (rewritten) |
| PC-71 | No fingerprint config → empty list (not "no value")                                                                                                         | 6.8 (U)          | FS-1                                       |
| PC-72 | Header + rows with localised names and counts; multi-section order; no duplication on view re-creation; toolbar up                                          | 3.8 (C), 6.8 (U) | `FingerSelectionFragmentTest`, FS-2, FS-3  |
| PC-73 | **KI-7:** unknown finger (`TemplateIdentifier.NONE`) is kept in the section and `toString(context)` throws `IllegalArgumentException` (crash while drawing) | 6.8 (U)          | FS-4 (pins current behaviour, step 2)      |

### 5.7 Logout screens

| ID    | Behaviour                                                                                                                                       | Audit                   | Pinned by                                         |
|-------|-------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------|---------------------------------------------------|
| PC-80 | "Logout without sync" visible iff events not complete, images syncing or data pending; 1 s anti-jitter                                          | 3.10 (C), 6.10 (U)      | `LogoutSyncViewModelTest` (rewritten), LV-6       |
| PC-81 | Button and info text shown/hidden on each value, including true → false; XML defaults before the first value                                    | 3.10, 4 (C/W), 6.10 (U) | `LogoutSyncFragmentTest`, LV-3, N-10              |
| PC-82 | Button → `logOutSyncDeclineFragment`; toolbar up on both screens                                                                                | 6.10 (U)                | LV-2, LV-7                                        |
| PC-83 | Decline: no password → confirmation → exact `Logout.start()`; password → correct → logout; wrong → nothing; cancel (button, dialog) → no logout | 3.10, 4 (C/W), 6.10 (U) | `LogoutSyncDeclineFragmentTest`, LV-1, LV-4, LV-5 |
| PC-84 | Password config read at confirm time                                                                                                            | 3.10 (C), 6.10          | `LogoutSyncDeclineViewModelTest`, LV-8            |
| PC-85 | Pre-logout panel runs SyncInfo in pre-logout mode                                                                                               | 6.10 (U)                | S-3, S-7 (LV-9)                                   |

### 5.8 Debug — not applicable

The Debug screen is removed by human decision (2026-11-24, D-11). Former PC-90…PC-93 (debug commands, sync-state line, clean-all order,
print DB) have no successor. Parity item after step 11: the main menu shows only Privacy Notice (when consent is required) and Settings,
in every build type, and `debugFragment` no longer exists in `graph_dashboard`.

### 5.9 Cross-cutting

| ID     | Behaviour                                                                                                                                                                                                           | Pinned by                                        |
|--------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------|
| PC-100 | Loading: no screen shows a spinner of its own; pre-load appearance = XML defaults (D-3). Error/retry: the only retry is SyncInfo's "try again" (same action as sync now, PC-30, S-8). No other screen has error UI. | D-3 tests per screen (M-3, P-3, N-10, ST-1), S-8 |
| PC-101 | Back handling: no `OnBackPressedCallback` exists; system back = Navigation default; every toolbar up / cancel = `popBackStack()`                                                                                    | ST-8, AB-8, FS-3, LV-5, LV-7, S-12               |
| PC-102 | Process death: no `SavedStateHandle` before or after; `RequestLoginFragment` flag unchanged; effects are user-initiated (D-6)                                                                                       | RL-2                                             |
| PC-103 | Event logging: only `DeviceConfigurationUpdatedEvent` (DEVICE scope, local) inside the device-config transform, before the language output; no `SessionEventRepository` calls exist                                 | `SettingsViewModelTest`, ST-6                    |
| PC-104 | `Simber` call sites and messages unchanged: `DashboardActivity.onLogout` (i), `MainViewModel` rooted (e), `SettingsViewModel` language (i, `SETTINGS`), `AboutViewModel` pending events (i, `SYNC`)                 | Code review (reviewer); no new logging API       |
| PC-105 | Double taps: sync now twice → two restarts (as today); re-login twice → two navigations (KI-1); confirm twice → two dialogs requested                                                                               | N-9 (SyncInfo), KI-1 parity cases                |

---

## 6. Contract stability map

`module-contracts.md` does not apply to this module (no `Contract`, `StepParams`, `StepResult`, `finishWithResult`, or orchestrator
binding; `orchestratorSerializersModule` contains nothing from dashboard). The module boundary below is frozen for Phase 1.

| Item                    | Value (must stay identical)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
|-------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Nav graph               | `R/navigation/graph_dashboard.xml`: graph `dashboard_navigation` (start `baseFragment`); destinations `requestLoginFragment`, `baseFragment`, `mainFragment`, `settingsFragment`, `aboutFragment`, `syncInfoFragment`, `fingerSelectionFragment`, nested `logout_navigation` (`logOutSyncFragment`, `logOutSyncDeclineFragment`); `android:name` class names unchanged (no Fragment is renamed or moved). **Approved exception:** `debugFragment` and `action_mainFragment_to_debugFragment` are removed in step 11 (D-11; dashboard is exempt from `module-contracts.md`, no reference outside the module, human decision 2026-11-24).                                                     |
| Actions                 | `action_requestLoginFragment_to_mainFragment`, `action_requestLoginFragment_to_troubleshooting`, `action_baseFragment_to_mainFragment`, `action_mainFragment_to_{settingsFragment,syncInfoFragment,privacyNoticesFragment,login}` (`…_to_debugFragment` removed, step 11), `action_syncInfoFragment_to_moduleSelection` (declared on three destinations), `action_settingsFragment_to_{aboutFragment,syncInfoFragment,fingerSelectionFragment}`, `action_aboutFragment_to_{logout_navigation,troubleshooting}`, `action_syncInfoFragment_to_login`, `action_logoutSyncFragment_to_logoutSyncDeclineFragment`, `action_logOutSyncFragment_to_login`, global `action_to_requestLoginFragment` |
| Included graphs         | `graph_privacy`, `graph_login`, `graph_troubleshooting`, `graph_module_selection` — includes unchanged                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| Nav argument            | `requestLoginFragment.logoutReason: LogoutReason?` (`@Keep @Parcelize`, fields `title`, `body`), built only by `DashboardActivity.onLogout`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| Outgoing params         | `LoginParams(projectId, userId)` (`:feature:login`) passed as `toBundle()` to `login.R.id.graph_login`; construction rule unchanged (PC-40)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| Results received        | `handleResult<ModuleSelectorResult>(…, currentDestinationId, ModuleSelectorContract.DESTINATION)` and `handleResult<LoginResult>(…, LoginContract.DESTINATION)` in `SyncInfoFragment.onViewCreated`, `viewLifecycleOwner`; current-destination rule unchanged (PC-42)                                                                                                                                                                                                                                                                                                                                                                                                                       |
| Results returned        | none (`finishWithResult` is not used; must stay unused)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| Activity                | `DashboardActivity` public, manifest entry and `portrait` orientation unchanged; `onLogout` unchanged                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| Layouts / resources     | All XML layouts, menus, preference XMLs, `attrs.xml` (`SyncFragmentContainerView`) stay; only the unreferenced `fragment_settings_password_input.xml` is deleted (step 12)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| Hilt                    | `@AndroidEntryPoint` on every Fragment/Activity; ViewModels `@HiltViewModel` + `@Inject constructor`; no new Hilt module; no ViewModel injected into another                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| Shared `:infra:ui-base` | Additive only: new package `viewmodel`; `NavigationResultExt`, `SettingsPasswordDialogFragment` untouched                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |

---

## 7. Test coverage delta plan

### 7.1 File-by-file

`TCR` = `TestCoroutineRule`; `ITER` = `InstantTaskExecutorRule`. "Before" = written first against the current code and run green in the
same step, then kept (re-pointed if it touches the ViewModel API) through the refactor.

| Step | File                                                                           | Tests to add / strengthen (behaviour proven)                                                                                                                                                                                                             | ITER after step            |
|------|--------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------|
| 1    | `infra/ui-base/.../viewmodel/BaseMviViewModelTest.kt` (new)                    | B-1…B-9 exactly as specified in `mvi-base-classes.md` §5                                                                                                                                                                                                 | n/a                        |
| 1    | `T/tools/MviTestExt.kt` (new helper)                                           | `stubMvi` as specified in `mvi-base-classes.md` §6                                                                                                                                                                                                       | n/a                        |
| 1    | `T/DashboardActivityTest.kt` (new)                                             | A-1, A-2 (before), A-3                                                                                                                                                                                                                                   | n/a                        |
| 1    | `T/requestlogin/RequestLoginFragmentTest.kt`                                   | RL-1, RL-2, RL-3 (before), RL-4                                                                                                                                                                                                                          | n/a                        |
| 2    | `T/settings/fingerselection/FingerSelectionViewModelTest.kt`                   | Rewrite 3 tests on `uiState`; FS-1, FS-4 VM half (before: `NONE` kept in the section, KI-7); N-12 aggregation produces equal immutable items                                                                                                             | **dropped**                |
| 2    | `T/settings/fingerselection/FingerSelectionFragmentTest.kt`                    | Re-point to `stubMvi`; FS-2 (before), FS-3 (re-creation sends `Load` again and renders one copy; toolbar up)                                                                                                                                             | n/a                        |
| 2    | `T/settings/fingerselection/FingerSelectionItemAdapterTest.kt` (new)           | FS-4 adapter half (before): `TemplateIdentifier.NONE.toString(context)` throws `IllegalArgumentException` (KI-7; relaxed `Context` mock suffices)                                                                                                        | n/a                        |
| 3    | `T/main/projectdetails/ProjectDetailsViewModelTest.kt`                         | Rewrite 2 tests (strict mocks kept); P-4 VM half (before), P-5, P-6, N-11 (init + resume loads sequential; last wins)                                                                                                                                    | **dropped**                |
| 3    | `T/main/projectdetails/ProjectDetailsFragmentTest.kt`                          | Re-point; P-1, P-2, P-3 (before), P-4 Fragment half (resume → `Load`), D-3 rule: `isLoaded == null` leaves card visible                                                                                                                                  | n/a                        |
| 3    | `T/main/MainFragmentTest.kt`                                                   | `projectDetailsViewModel` stubs moved to `stubMvi` (no assertion change)                                                                                                                                                                                 | n/a                        |
| 4    | `T/main/dailyactivity/DailyActivityViewModelTest.kt`                           | Rewrite 2 tests (date now in state); D-2 VM half (before); divider/has-activity rules on `DailyActivityUi`                                                                                                                                               | **dropped**                |
| 4    | `T/main/dailyactivity/DailyActivityFragmentTest.kt`                            | Re-point 16 tests; D-1 (before), D-2 Fragment half, D-3                                                                                                                                                                                                  | n/a                        |
| 4    | `T/main/MainFragmentTest.kt`                                                   | `dailyActivityViewModel` stubs moved to `stubMvi`                                                                                                                                                                                                        | n/a                        |
| 5    | `T/main/MainViewModelTest.kt`                                                  | Rewrite 2 tests; M-4, M-5 (before; KI-6: config read throws → root check never called, overlay state stays false, exception not swallowed)                                                                                                               | **dropped**                |
| 5    | `T/main/MainFragmentTest.kt`                                                   | Re-point 5 tests; M-1, M-2, M-3 (before; M-2 recreates the view and asserts the overlay again)                                                                                                                                                           | n/a                        |
| 6    | `T/logout/syncdecline/LogoutSyncDeclineViewModelTest.kt` (new)                 | Moved "password config fetched" (now: `ConfirmClicked` emits the right effect for both configs); LV-1 exact `Logout.start()`; LV-8 (config changed between two clicks)                                                                                   | n/a (none)                 |
| 6    | `T/logout/syncdecline/LogoutSyncDeclineFragmentTest.kt`                        | Re-point 4 tests to effects/actions; LV-4, LV-5 (before), LV-7 (decline half)                                                                                                                                                                            | n/a                        |
| 6    | `T/logout/LogoutSyncViewModelTest.kt`                                          | Remove the two moved tests (no assertion lost: moved to the new class)                                                                                                                                                                                   | kept (LiveData remains)    |
| 7    | `T/logout/sync/LogoutSyncViewModelTest.kt` (moved from `T/logout/`)            | Rewrite 4 visibility tests on `uiState` (Turbine); LV-6 (`advanceTimeBy`); delete the 2 `logoutEventLiveData` tests (dead code, approved removal; logout terminal pinned by A-1/A-2)                                                                     | **dropped**                |
| 7    | `T/logout/sync/LogoutSyncFragmentTest.kt`                                      | Re-point 2 tests (info text asserted too); LV-2, LV-3 (before), LV-7 (sync half), N-10 (`null` → XML defaults)                                                                                                                                           | n/a                        |
| 8    | `T/settings/SettingsViewModelTest.kt`                                          | Rewrite 5 tests (jraska observers → Turbine); ST-6 (before), ST-9 VM half (KI-2), ST-10, N-4, N-5, N-8                                                                                                                                                   | **dropped**                |
| 8    | `T/settings/SettingsFragmentTest.kt`                                           | Re-point 7 tests; ST-1…ST-5 (before), ST-7, ST-8, ST-9 Fragment half (before, with a real `MutableLiveData<LiveDataEventWithContent>`; re-pointed to the effect channel), ST-11 (re-render hides fingerprint prefs)                                      | n/a                        |
| 9    | `T/settings/about/AboutViewModelTest.kt`                                       | Rewrite 12 tests; AB-1, AB-2 (before), AB-7, N-6, N-7                                                                                                                                                                                                    | **dropped**                |
| 9    | `T/settings/about/AboutFragmentTest.kt`                                        | Re-point 9 tests; AB-3, AB-4, AB-5 (before), AB-6, AB-8, AB-9 (scanner pref visible → hidden re-render)                                                                                                                                                  | n/a                        |
| 10   | `T/settings/syncinfo/SyncInfoViewModelTest.kt`                                 | Rewrite 37 tests on `uiState` (subscribe first: `uiState` is `WhileSubscribed`), remove `mockkStatic(FlowLiveDataConversions)` and the connectivity LiveData stub; S-1, S-2, S-3, S-4 (before); S-5 per KI-1 (VM half after: `seq`); S-16; N-1, N-3, N-9 | **dropped**                |
| 10   | `T/settings/syncinfo/SyncInfoFragmentTest.kt` (new)                            | S-6…S-12 (before, mock with real `MutableLiveData`s; re-pointed to `stubMvi`); KI-1 Fragment parity cases (before: real `MutableLiveData<LoginParams>`; after: `loginRequest` state); S-13; S-7 also verifies `HostAttached(<expected>)` per host        | n/a                        |
| 10   | `T/settings/syncinfo/SyncInfoRenderRulesTest.kt` (new)                         | N-2: every rule in `SyncInfoRenderRules` with both host configs where relevant                                                                                                                                                                           | n/a                        |
| 10   | `T/settings/syncinfo/usecase/internal/GetSyncInfoSectionRecordsUseCaseTest.kt` | S-14                                                                                                                                                                                                                                                     | n/a                        |
| 10   | `T/settings/syncinfo/usecase/ObserveSyncInfoUseCaseTest.kt`                    | S-15                                                                                                                                                                                                                                                     | **dropped** (never needed) |
| 10   | `T/tools/NavigationResultTestExt.kt` (new helper)                              | Seeds `"result-<destinationId>"` on a back-stack entry `SavedStateHandle` for S-6                                                                                                                                                                        | n/a                        |
| 11   | `T/main/MainFragmentTest.kt`                                                   | Delete `should redirect to the debug fragment when clicking on debug` (audit 3.2 Covered; behaviour removed by human decision). No other change                                                                                                          | n/a                        |
| 12   | —                                                                              | No test changes except deleting `T/tools/Espresso.kt`                                                                                                                                                                                                    | none left in module        |

**Parity risks from P0 halves that can only be written after the refactor** (executor workflow step 2): the ViewModel half of S-5 (`seq`
does not exist before) and of ST-9 (effect type does not exist before). Both are mitigated because their Fragment halves are written before
the refactor against real `MutableLiveData` instances and keep running after it.

**New tests identified by this plan** (not in the audit): B-1…B-9 (base class), N-1 (stream waits for `HostAttached`; `observeSyncInfo`
invoked once with the attached value), N-2 (render rules), N-3 (≥ 5 s collector gap: last `SyncInfo` re-delivered first, initial-sync
decision re-run), N-4 (language persist not blocked by refresh), N-5 (initial `ShowConfigLastUpdated` delivered to a late collector),
N-6 (queue survives `CancellationException`), N-7 (About gate decisions), N-8 (Settings gate decisions), N-9 (double sync tap → two
restarts), N-10 (logout visibility `null` keeps XML defaults), N-11 (card loads sequential), N-12 (immutable aggregation), N-13 (debug line
mapping), ST-11, AB-9 (visible → hidden re-renders).

### 7.2 Audit disposition

| ID         | Pri | Disposition                                                                                         |
|------------|-----|-----------------------------------------------------------------------------------------------------|
| A-1        | P0  | Scheduled, step 1                                                                                   |
| A-2        | P0  | Scheduled, step 1                                                                                   |
| A-3        | P1  | Scheduled, step 1 (same class; guards the terminal step of every logout flow)                       |
| A-4        | P2  | Deferred: the Activity and the storage-alert call are not touched in Phase 1                        |
| M-1…M-4    | P0  | Scheduled, step 5                                                                                   |
| M-5        | P1  | Scheduled: pins current behaviour per KI-6 (step 5)                                                 |
| M-6        | P2  | Not applicable: the debug menu item is removed with the Debug screen (human decision 2026-11-24)    |
| P-1…P-4    | P0  | Scheduled, step 3                                                                                   |
| P-5, P-6   | P1  | Scheduled, step 3 (load rewritten)                                                                  |
| D-1, D-2   | P0  | Scheduled, step 4                                                                                   |
| D-3        | P1  | Scheduled, step 4 (render rewritten)                                                                |
| S-1…S-4    | P0  | Scheduled, step 10                                                                                  |
| S-5        | P0  | Scheduled, step 10, **with the KI-1 wording** (pins replay; supersedes the audit's "delivers once") |
| S-6…S-12   | P0  | Scheduled, step 10                                                                                  |
| S-13…S-16  | P1  | Scheduled, step 10 (S-14/S-15 cover fields the new render rules read)                               |
| S-17       | P2  | Deferred: `ModuleCountAdapter`/`ViewHolder` unchanged in Phase 1                                    |
| S-18       | P2  | Deferred: `ObserveSyncInfoUseCase` unchanged in Phase 1                                             |
| ST-1…ST-6  | P0  | Scheduled, step 8                                                                                  |
| ST-7, ST-8 | P1  | Scheduled, step 8                                                                                  |
| ST-9       | P1  | Scheduled, step 8, **final wording from KI-2**                                                     |
| ST-10      | P2  | Scheduled, step 8: KI-2 makes it part of ST-9 ("a refresh that emits nothing produces neither")    |
| AB-1…AB-5  | P0  | Scheduled, step 9                                                                                  |
| AB-6…AB-8  | P1  | Scheduled, step 9                                                                                  |
| FS-1, FS-2 | P0  | Scheduled, step 2                                                                                   |
| FS-3       | P1  | Scheduled, step 2                                                                                   |
| FS-4       | P2  | Scheduled: pins current behaviour per KI-7 (step 2)                                                 |
| RL-1…RL-3  | P0  | Scheduled, step 1                                                                                   |
| RL-4       | P1  | Scheduled, step 1                                                                                   |
| LV-1       | P0  | Scheduled, step 6                                                                                   |
| LV-2, LV-3 | P0  | Scheduled, step 7                                                                                   |
| LV-4, LV-5 | P0  | Scheduled, step 6                                                                                   |
| LV-6       | P1  | Scheduled, step 7                                                                                   |
| LV-7       | P1  | Scheduled, decline half step 6, sync half step 7                                                    |
| LV-8       | P1  | Scheduled, step 6                                                                                   |
| LV-9       | P1  | Scheduled, step 10 (delivered by S-3 and the logout-host case of S-7)                               |
| DB-1…DB-3  | P1  | Not applicable: screen removed by human decision (2026-11-24)                                       |
| DB-4       | P2  | Not applicable: screen removed by human decision (2026-11-24)                                       |

### 7.3 `Weak` and `Uncovered` closure

| Audit row                                                                                                                                       | Rating | Closed by                                |
|-------------------------------------------------------------------------------------------------------------------------------------------------|--------|------------------------------------------|
| 3.3 scanner text (card hidden in test)                                                                                                          | W      | P-1                                      |
| 4 scanner hidden when empty                                                                                                                     | W      | P-2                                      |
| 4 daily card hidden / groups hidden / dividers hidden                                                                                           | W      | D-1 (+ re-pointed tests push two states) |
| 3.5 auto-logout command; `performLogout`                                                                                                        | W      | S-2                                      |
| 3.5 login navigation `isNotNull`                                                                                                                | W      | S-5 (KI-1)                               |
| 4 finger pref hidden (XML default)                                                                                                              | W      | ST-11                                    |
| 3.7 About logout command                                                                                                                        | W      | AB-1                                     |
| 4 About scanner hidden (XML default)                                                                                                            | W      | AB-9                                     |
| 3.10 `logout()` command                                                                                                                         | W      | LV-1                                     |
| 4 logout info text hidden (`invisible` default)                                                                                                 | W      | LV-3                                     |
| 5.1 logout broadcast → request login                                                                                                            | U      | A-1, A-2                                 |
| 5.1 request login → troubleshooting                                                                                                             | U      | RL-3                                     |
| 5.1 about → troubleshooting                                                                                                                     | U      | AB-4                                     |
| 5.1 logout sync → decline                                                                                                                       | U      | LV-2                                     |
| 5.1 sync info → module selection; card → `syncInfoFragment`; → `graph_login`                                                                    | U      | S-9, S-12, KI-1 cases                    |
| 5.1 toolbar up / cancel                                                                                                                         | U      | ST-8, AB-8, FS-3, LV-5, LV-7, S-12       |
| 5.1 language relaunch                                                                                                                           | U      | ST-3                                     |
| 5.1 external intents                                                                                                                            | U      | S-12                                     |
| 5.2 result wiring and back-stack entry selection                                                                                                | U      | S-6                                      |
| 5.4 `Images.start()` after events (pre-logout)                                                                                                  | U      | S-1                                      |
| 5.4 Debug commands                                                                                                                              | U      | Not applicable: screen removed (D-11)    |
| 5.5 `KEY_LOGOUT_REASON`; nav arg                                                                                                                | U      | RL-2; RL-1                               |
| 5.5 `isPreLogoutUpSync` timing                                                                                                                  | U      | S-3, N-1, S-7                            |
| 5.5 unlocked settings reuse                                                                                                                     | U      | ST-7, AB-5                               |
| 6.x rows without an audit proposal (CommCare label, device ID copy, plurals, update-config Fragment flow, visibility re-render, popup priority) | U      | AB-7, AB-6, D-3, ST-5, LV-3, S-10        |

The two `logoutEventLiveData` tests (3.10, "Covered, for a stream nothing in production consumes") are deleted with the dead member in step
9
— an explicit, approved dead-code removal; the logout outcome they approximated is pinned at the real terminal (A-1, A-2).

`MainFragmentTest::should redirect to the debug fragment when clicking on debug` (3.2, Covered) is deleted in step 11 together with the
behaviour it pinned, by human decision (2026-11-24).

---

## 8. Ordered execution plan

**Rules for every step.** One step = one commit, staged by the explicit paths listed. Inside a screen step the order is: (1) write the
step's "before" P0 tests against the current code and run them green; (2) add the `<Screen>MVI.kt` contracts; (3) migrate the ViewModel; (4)
make the Fragment state/effect-driven; (5) re-point and extend tests; (6) delete dead code. Nothing from another screen, except the
listed host-test stub edits in `MainFragmentTest`. Every step ends with:

```
./gradlew :feature:dashboard:test :feature:dashboard:kspDebugKotlin :feature:dashboard:lintDebug
```

**Stop conditions common to every step:** a "before" test fails against the current code (stop: either the audit description or the test is
wrong; report, do not adapt production); a covered behaviour can only be kept by changing a nav ID, action, argument, result key or
`LoginParams` construction (stop); a step needs a file not listed here (stop and report the file, unless it is a pure import fix in a
listed file); any test count drops other than the deletions listed in the step.

### Step 1 — `MS-1440 Dashboard P1: MVI foundation and pre-refactor tests`

One commit with three parts: (a) the shared MVI base class and test tooling, (b) the `DashboardActivity` logout routing tests and (c) the
`RequestLogin` tests. Parts (b) and (c) are test-only and pin the screens that end a logout before any screen is refactored. The only
production change is the new `BaseMviViewModel` in `:infra:ui-base`; nothing in `:feature:dashboard/src/main` changes (D-9).

**(a) BaseMviViewModel and MVI test tooling.** Implement exactly what `docs/migration/mvi-base-classes.md` specifies (sections 1–6); it is
the source of truth for this part.

- **Create**
    - `infra/ui-base/src/main/java/com/simprints/infra/uibase/viewmodel/BaseMviViewModel.kt` (spec §2–§4)
    - `infra/ui-base/src/test/java/com/simprints/infra/uibase/viewmodel/BaseMviViewModelTest.kt` (spec §5, B-1…B-9)
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/tools/MviTestExt.kt` (spec §6)
- **Stop if**: anything in the spec cannot be met as written (report the spec section; do not improvise). Record which backing-field form
  (spec §4) was used.

**(b) DashboardActivity logout routing tests.**

- **Create**: `feature/dashboard/src/test/java/com/simprints/feature/dashboard/DashboardActivityTest.kt` (A-1, A-2, A-3; `@BindValue`
  relaxed `ShowStorageAlertIfNecessaryUseCase`). No production change; `BaseFragment` unchanged.
- **Stop if**: `DashboardActivity` cannot be launched under Robolectric + Hilt. Fallback allowed without asking: drive
  `onLogout(isProjectEnded)` through `ActivityScenario.onActivity` (the broadcast → `onLogout` hop lives in `:infra:core`); record it.

**(c) RequestLogin.**

- **Modify**: `feature/dashboard/src/test/java/com/simprints/feature/dashboard/requestlogin/RequestLoginFragmentTest.kt` (RL-1…RL-4).
  No production change (D-9).

**Modify / delete** outside the files above: none, except the catalog status row (`in-progress`).

**Validate**: `./gradlew :infra:ui-base:test :infra:ui-base:lintDebug :feature:dashboard:test :feature:dashboard:kspDebugKotlin
:feature:dashboard:lintDebug :feature:orchestrator:test` (the last one as a smoke check because `:infra:ui-base` is shared; not required by
`module-contracts.md` for this module).

> **Note (2026-11-24):** this step was originally planned as three commits (base class; `DashboardActivity` tests; `RequestLogin` tests)
> and was merged into one by human decision after they were executed. Later steps were renumbered from 4–14 to 2–12.

### Step 2 — `MS-1440 Dashboard P1: FingerSelection`

- **Create**: `feature/dashboard/src/main/java/com/simprints/feature/dashboard/settings/fingerselection/FingerSelectionMVI.kt`
- **Modify**
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/settings/fingerselection/FingerSelectionViewModel.kt`
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/settings/fingerselection/FingerSelectionFragment.kt`
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/settings/fingerselection/FingerSelectionItemAdapter.kt`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/settings/fingerselection/FingerSelectionViewModelTest.kt`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/settings/fingerselection/FingerSelectionFragmentTest.kt`
- **Create (test)**:
  `feature/dashboard/src/test/java/com/simprints/feature/dashboard/settings/fingerselection/FingerSelectionItemAdapterTest.kt`
- **Delivers**: FS-1, FS-2, FS-4 (before; FS-4 pins KI-7), FS-3, N-12. **Stop if** the immutable aggregation changes order or counts for any
  existing test input, or drops `NONE` entries.

### Step 3 — `MS-1440 Dashboard P1: ProjectDetails card`

- **Create**: `feature/dashboard/src/main/java/com/simprints/feature/dashboard/main/projectdetails/ProjectDetailsMVI.kt`
- **Modify**
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/main/projectdetails/ProjectDetailsViewModel.kt`
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/main/projectdetails/ProjectDetailsFragment.kt`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/main/projectdetails/ProjectDetailsViewModelTest.kt`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/main/projectdetails/ProjectDetailsFragmentTest.kt`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/main/MainFragmentTest.kt` (stub only, the card is hosted by
      `MainFragment`)
- **Delete**: `feature/dashboard/src/main/java/com/simprints/feature/dashboard/main/projectdetails/DashboardProjectState.kt`
- **Delivers**: P-1…P-4 (before), P-5, P-6, N-11.

### Step 4 — `MS-1440 Dashboard P1: DailyActivity card`

- **Create**: `feature/dashboard/src/main/java/com/simprints/feature/dashboard/main/dailyactivity/DailyActivityMVI.kt`
- **Modify**
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/main/dailyactivity/DailyActivityViewModel.kt`
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/main/dailyactivity/DailyActivityFragment.kt`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/main/dailyactivity/DailyActivityViewModelTest.kt`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/main/dailyactivity/DailyActivityFragmentTest.kt`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/main/MainFragmentTest.kt` (stub only)
- **Delete**: `feature/dashboard/src/main/java/com/simprints/feature/dashboard/main/dailyactivity/DashboardDailyActivityState.kt`
- **Delivers**: D-1, D-2 (before), D-3.

### Step 5 — `MS-1440 Dashboard P1: Main`

- **Create**: `feature/dashboard/src/main/java/com/simprints/feature/dashboard/main/MainMVI.kt`
- **Modify**
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/main/MainViewModel.kt`
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/main/MainFragment.kt`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/main/MainViewModelTest.kt`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/main/MainFragmentTest.kt`
- **Delivers**: M-1…M-5 (before; M-5 pins KI-6). The debug menu item, its `BuildConfig.DEBUG` wiring and the debug-navigation test stay
  unchanged here; step 11 removes them. **Stop if** M-2 shows the overlay is not sticky on the current code (the audit's reading would be
  wrong;
  report before modelling).

### Step 6 — `MS-1440 Dashboard P1: LogoutSyncDecline`

Converted before `LogoutSync` because it removes the decline-only members from the shared class; doing it the other way round would leave
LiveData members on an MVI ViewModel for one commit.

- **Create**
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/logout/syncdecline/LogoutSyncDeclineMVI.kt`
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/logout/syncdecline/LogoutSyncDeclineViewModel.kt`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/logout/syncdecline/LogoutSyncDeclineViewModelTest.kt`
- **Modify**
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/logout/syncdecline/LogoutSyncDeclineFragment.kt`
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/logout/LogoutSyncViewModel.kt` (remove `settingsLocked`, `logout()`,
      `ConfigRepository`)
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/logout/syncdecline/LogoutSyncDeclineFragmentTest.kt`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/logout/LogoutSyncViewModelTest.kt` (remove the 2 moved tests)
- **Delivers**: LV-4, LV-5 (before), LV-1, LV-8, LV-7 (decline).

### Step 7 — `MS-1440 Dashboard P1: LogoutSync`

- **Create**: `feature/dashboard/src/main/java/com/simprints/feature/dashboard/logout/sync/LogoutSyncMVI.kt`
- **Move + modify** (`git mv`, then edit)
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/logout/LogoutSyncViewModel.kt` →
      `feature/dashboard/src/main/java/com/simprints/feature/dashboard/logout/sync/LogoutSyncViewModel.kt`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/logout/LogoutSyncViewModelTest.kt` →
      `feature/dashboard/src/test/java/com/simprints/feature/dashboard/logout/sync/LogoutSyncViewModelTest.kt`
- **Modify**
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/logout/sync/LogoutSyncFragment.kt`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/logout/sync/LogoutSyncFragmentTest.kt`
- **Dead code**: `logoutEventLiveData` and its 2 tests, `AuthStore` dependency.
- **Delivers**: LV-2, LV-3 (before), LV-6, LV-7 (sync), N-10.

### Step 8 — `MS-1440 Dashboard P1: Settings`

- **Create**: `feature/dashboard/src/main/java/com/simprints/feature/dashboard/settings/SettingsMVI.kt`
- **Modify**
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/settings/SettingsViewModel.kt`
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/settings/SettingsFragment.kt`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/settings/SettingsViewModelTest.kt`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/settings/SettingsFragmentTest.kt`
- **Delivers**: ST-1…ST-6 (before), ST-7, ST-8, ST-9 (KI-2), ST-10, ST-11, N-4, N-5, N-8.
- **Stop if**: ST-3 shows the relaunch cannot be kept in today's order without blocking the persist (report; do not switch to the DV-3
  follow-up on your own); the KI-2 effect order (toast, then summary) cannot be met.

### Step 9 — `MS-1440 Dashboard P1: About`

- **Create**: `feature/dashboard/src/main/java/com/simprints/feature/dashboard/settings/about/AboutMVI.kt`
- **Modify**
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/settings/about/AboutViewModel.kt`
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/settings/about/AboutFragment.kt`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/settings/about/AboutViewModelTest.kt`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/settings/about/AboutFragmentTest.kt`
- **Delete**: `feature/dashboard/src/main/java/com/simprints/feature/dashboard/settings/about/LogoutDestination.kt`
- **Delivers**: AB-1…AB-5 (before), AB-6…AB-9, N-6, N-7.

### Step 10 — `MS-1440 Dashboard P1: SyncInfo`

One step for all three hosts: there is one `SyncInfoFragment` class and one `SyncInfoViewModel` class; the hosts only contribute container
attributes. Placed after `Main` (step 5) and `LogoutSync` (step 7) so S-6/S-7/S-9, which launch those hosts, stub the host ViewModels in
their final MVI form and are written once. No host production file changes.

- **Create**
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/settings/syncinfo/SyncInfoMVI.kt`
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/settings/syncinfo/SyncInfoRenderRules.kt`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/settings/syncinfo/SyncInfoFragmentTest.kt`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/settings/syncinfo/SyncInfoRenderRulesTest.kt`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/tools/NavigationResultTestExt.kt`
- **Modify**
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/settings/syncinfo/SyncInfoViewModel.kt`
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/settings/syncinfo/SyncInfoFragment.kt`
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/settings/syncinfo/SyncInfo.kt` (delete `LogoutActionReason`)
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/settings/syncinfo/SyncInfoViewModelTest.kt`
    -
  `feature/dashboard/src/test/java/com/simprints/feature/dashboard/settings/syncinfo/usecase/internal/GetSyncInfoSectionRecordsUseCaseTest.kt`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/settings/syncinfo/usecase/ObserveSyncInfoUseCaseTest.kt`
- **Delivers**: S-1…S-12 (before; S-5 per KI-1), S-13…S-16, KI-1 Fragment parity cases, N-1, N-2, N-3, N-9, LV-9.
- **Test setup note**: `SyncInfoFragmentTest` binds the `SyncInfoViewModel` mock plus relaxed mocks of the host ViewModels (`MainViewModel`,
  `ProjectDetailsViewModel`, `DailyActivityViewModel`, `LogoutSyncViewModel`) stubbed with `stubMvi`, so S-6/S-7/S-9 can
  launch `MainFragment` and `LogoutSyncFragment`. View re-creation for the KI-1 cases uses activity recreation with the same bound mock;
  stop/start uses `moveToState` from `HiltExt`.
- **Stop if**: any S-6…S-12 "before" test disagrees with the audit's description of the Fragment; the KI-1 collector cannot meet all four
  parity cases; `MainFragmentTest` or `LogoutSyncFragmentTest` (real `SyncInfoViewModel`) fail after the refactor; the
  `SyncInfoViewModelTest` debounce timings (2900/200/3100/4100 ms) need different values (that would mean the 3 s delay or 5 s stop
  timeout changed).

### Step 11 — `MS-1440 Dashboard P1: remove Debug screen`

Human decision (2026-11-24, D-11): remove `DebugFragment` from the application. Placed after `Main` (step 5) so `MainFragment` and
`MainFragmentTest` are edited in their final MVI form; the debug item is plain static navigation until then.

- **Delete**
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/debug/DebugFragment.kt` (the `debug/` package becomes empty)
    - `feature/dashboard/src/main/res/layout/fragment_debug.xml`
- **Modify**
    - `feature/dashboard/src/main/res/navigation/graph_dashboard.xml`: remove the `debugFragment` destination and the
      `action_mainFragment_to_debugFragment` action (approved exception to "nav IDs unchanged", section 6)
    - `feature/dashboard/src/main/res/menu/app_menu.xml`: remove the `debug` item
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/main/MainFragment.kt`: remove the `!BuildConfig.DEBUG` block, the
      `R.id.debug` branch and the now-unused `BuildConfig` import
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/main/MainFragmentTest.kt`: delete
      `should redirect to the debug fragment when clicking on debug`
    - `docs/migration/migration-catalog.md` §2, dashboard row: remove `DebugFragment` from the screen list and change
      `graph_dashboard.xml (10 destinations)` to `graph_dashboard.xml (9 destinations)`
- **Nothing else references it** (repository-wide grep for `DebugFragment`, `debugFragment`, `fragment_debug`, `FragmentDebugBinding`,
  `R.id.debug`, `DebugActivitySyncState` at the baseline): no strings, drawables or dimensions (labels are hard-coded), no Hilt module or
  binding of its own, no other module.
- **Debug-only dependency left unused (follow-up, do not remove here):** `implementation(libs.workManager.work)` in
  `feature/dashboard/build.gradle.kts` — `DebugFragment` is the only `src/main` file importing `androidx.work`. Every other dependency
  it used (`:infra:events`, `:infra:event-sync`, `:infra:enrolment-records:repository`, `:infra:sync`, `:infra:auth-store`) is still
  used by other dashboard code. `debugImplementation(project(":testing:data-generator"))` is unrelated to this screen and unchanged.
- **Stop if**: any reference to the removed IDs, layout or class remains after the edits; lint reports a newly unused resource that
  belongs to another module.
- **Validate**: the common commands, plus `grep -rn "debugFragment\|DebugFragment\|fragment_debug\|R.id.debug\b" feature/dashboard`
  returns nothing.

### Step 12 — `MS-1440 Dashboard P1: dead-code cleanup`

- **Delete**
    - `feature/dashboard/src/main/java/com/simprints/feature/dashboard/tools/Fuzzy.kt`
    - `feature/dashboard/src/main/res/layout/fragment_settings_password_input.xml`
    - `feature/dashboard/src/test/java/com/simprints/feature/dashboard/tools/Espresso.kt`
- **Modify**: `feature/dashboard/build.gradle.kts` (remove `implementation(libs.fuzzywuzzy.core)`)
- **Not touched**: `gradle/libs.versions.toml` (`fuzzywuzzy_version`, `fuzzywuzzy-core` become unused; removing them is a shared-file
  change, listed as a follow-up).
- **Stop if**: lint reports `dashboard_password_lock_hint` (or another resource) as newly unused after the layout deletion and it is not
  used
  by another module (report; do not delete strings from `:infra:resources`).
- **Final checks**: `grep -rn "LiveData\|livedata\|InstantTaskExecutorRule\|getOrAwaitValue\|jraska" feature/dashboard/src` returns nothing.

---

## 9. Blocking risks and rollback plan

### 9.1 Risks

Nothing blocks planning. Items that need a human answer are in section 11.

| #     | Risk                                                                                                                                                                                                                         | Blocking?                | Mitigation                                                                                                         |
|-------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------|--------------------------------------------------------------------------------------------------------------------|
| BR-1  | **E2E gap.** Audit §9 is `NOT CHECKED`. The reviewer approved it for the pilot as a known gap.                                                                                                                               | **No** (approved gap)    | Acceptance criterion AC-9: complete the §9 Testiny/Appium mapping before Phase 1 is marked `done`                  |
| BR-2  | Pre-logout chain (R-1) and mode timing (R-2) regress silently                                                                                                                                                                | No                       | D-7 gating; S-1, S-2, S-3, N-1, S-7 before the refactor                                                            |
| BR-3  | SyncInfo render logic (R-3) changes while moved                                                                                                                                                                              | No                       | S-6…S-11 written against current code first; pure `SyncInfoRenderRulesTest`                                        |
| BR-4  | Sticky/one-time semantics (R-4): rooted overlay, KI-1, KI-2                                                                                                                                                                  | No                       | State for rooted (M-2); DV-1/DV-2 per known-issues with Fragment-level pinning written against real LiveData first |
| BR-5  | Language relaunch race (R-5) — inherited, kept (DV-3)                                                                                                                                                                        | No                       | `launchWork` for refresh, N-4, ST-3, ST-6                                                                          |
| BR-6  | Base class differs from the `viewmodel-udf.md` §3 sample (`CancellationException` guard, possible backing-field fallback)                                                                                                    | No (accepted 2026-11-24) | Specified in `mvi-base-classes.md`; B-8 test; record the backing-field form used                                   |
| BR-7  | `DashboardActivity` may not start under Robolectric + Hilt                                                                                                                                                                   | No                       | Step 1 fallback                                                                                                    |
| BR-8  | Timing-sensitive tests (SyncInfo debounces, About counter, config-change emission counts)                                                                                                                                    | No                       | Keep virtual-time values; step 10 stop condition                                                                   |
| BR-9  | Latent behaviours kept for parity and recorded as known issues: KI-3 (relaunch timing), KI-4 (restart side effects), KI-5 (dialog re-show, unconfirmed), KI-6 (config failure skips root check), KI-7 (unknown finger crash) | No                       | Pinned per known-issues; fixes are separate reviewed changes                                                       |
| BR-10 | `FingerSelectionItem` `var` → `val` changes a public type                                                                                                                                                                    | No                       | Only module-internal users (scanned)                                                                               |

### 9.2 Rollback plan

- Phase 1 is not feature-flagged; rollback is `git revert` of step commits.
- **Independent steps**: 2, 3, 4, 8, 9 can each be reverted alone. Steps 3 and 4 also edit `MainFragmentTest` stubs; if step 5 is
  already in, revert 5 first or resolve the stub conflict in that file only.
- **Ordered pairs**: revert 7 before 6 (step 7 moves the class step 6 trimmed). Revert 12 before anything it follows if a deleted file must
  come back.
- **Tests-only parts** (step 1 parts b and c) never need reverting for a production rollback.
- **Step 11** (Debug removal) can be reverted alone if step 5 is in place; it restores the Fragment, layout, nav entries, menu item and test
  (and the catalog row).
- **Step 1** has consumers from step 2 on; reverting it requires reverting every converted screen first. Reverting step 1 alone is not
  supported.
- **Before release**: if a regression is found after merge, revert the screen's step commit (the previous LiveData implementation comes
  back intact with its tests, because each step replaces exactly one screen).

---

## 10. Acceptance criteria and required validation commands

### 10.1 Acceptance criteria

- **AC-1** Every ViewModel in the module extends `BaseMviViewModel` with an `internal` `<Screen>MVI.kt` (`UiState`/`UiAction`/`UiEffect`);
  every UI → ViewModel input goes through `onAction`; no `LiveData`, `LiveDataEvent`, `LiveDataEventWithContent`, `asLiveData`, or
  `MutableSharedFlow` effects remain in `feature/dashboard/src/main`.
- **AC-2** Every Fragment collects with `repeatOnLifecycle(STARTED)` on `viewLifecycleOwner`, except the KI-1 collector (DV-1).
- **AC-3** Section 5 holds: every `PC-*` item is asserted by the named test, and every audit `Covered` behaviour is still asserted.
- **AC-4** Section 7.2: every P0 and scheduled P1 delivered; deferred items unchanged in code.
- **AC-5** Section 6 unchanged: nav graph IDs/actions/args, `LoginParams` construction, `handleResult` bindings, public types' visibility.
- **AC-5b** The Debug screen is gone: no `debugFragment` destination or action, no `debug` menu item, no `DebugFragment`/`fragment_debug`,
  and the catalog §2 dashboard row lists 9 destinations without `DebugFragment` (D-11, step 11).
- **AC-5c** KI-3 … KI-7 behaviours are unchanged and pinned by the tests the known-issues document names (ST-3/ST-6/N-4, N-3, RL-1/RL-2,
  M-5, FS-4); KI-5 stays a device check.
- **AC-6** KI-1 … KI-7 kept exactly as `feature-dashboard.p1-known-issues.md` describes, with the parity comments.
- **AC-7** No `InstantTaskExecutorRule` in any test class of the module; Turbine used for flow assertions.
- **AC-8** Each step was committed on its own with the listed files, and each commit passes the validation commands below.
- **AC-9** **Before Phase 1 is marked `done`**: complete audit §9 E2E mapping (Testiny cases and Appium tests per flow, filled in by a
  human). Recommended, not required: run the mapped cases for flows 3, 7, 10, 11, 14, 15, 16 and 17 (the flows with the highest R-1…R-6
  exposure) on a device against the final step.
- **AC-10** `migration-p1-3-reviewer` PASS, then a human marks Phase 1 `done` in the catalog.

### 10.2 Required validation commands

Per step (all must pass):

```
./gradlew :feature:dashboard:test
./gradlew :feature:dashboard:kspDebugKotlin
./gradlew :feature:dashboard:lintDebug
```

Step 1 additionally:

```
./gradlew :infra:ui-base:test :infra:ui-base:lintDebug
./gradlew :feature:orchestrator:test   # smoke only: shared module changed; dashboard is not orchestrated
```

`./gradlew :feature:orchestrator:test` is not required for the dashboard screen steps (`module-contracts.md` exemption).

---

## 11. Sign-off

- `Manual review status: APPROVED`
- **Needs reviewer acceptance**: deviations DV-1…DV-6 (section 4.0). DV-3, DV-4 and DV-6 are already decided (see notes below).
- **Follow-ups outside this handoff**: remove the unused `fuzzywuzzy` entries from `gradle/libs.versions.toml`; remove the unused
  `implementation(libs.workManager.work)` from `feature/dashboard/build.gradle.kts` after step 11; KI-1 … KI-7 fixes as separate
  reviewed changes; confirm KI-5 on a device.
- `Reviewer notes / edits`:
    - **Decided 2026-11-24 — Debug screen:** remove `DebugFragment` from the application entirely (replaces options A and B). Step 11 is
      the removal; DB-1…DB-4 and M-6 are not applicable; the nav destination removal is an approved exception (D-11, section 6).
    - **Decided 2026-11-24 — DV-3:** keep the language relaunch behaviour as it is; recorded as KI-3.
    - **Decided 2026-11-24 — DV-4:** the `CancellationException` guard is correct; the `viewmodel-udf.md` §3 sample stays unchanged.
      The base class and shared test helper are specified in `docs/migration/mvi-base-classes.md`, the source of truth for D-1 and step 1.
    - **Decided 2026-11-24 — known issues:** AP-3/DV-6 recorded as KI-4; the RequestLogin dialog re-show recorded as KI-5 (unconfirmed,
      parity item PC-7).
    - **Decided 2026-11-24 — M-5 and FS-4:** keep current behaviour in Phase 1, recorded as KI-6 and KI-7; pinned by M-5 (step 5) and
      FS-4 (step 2), parity items PC-13 and PC-73.
    - _(reviewer)_
