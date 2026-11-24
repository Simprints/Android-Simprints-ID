# `:feature:dashboard`: behaviour kept as-is in Phase 1

Two behaviours that the Phase 1 test audit (`feature-dashboard.p1-testing.md`, risk **R-4 (b)** and **(c)**) flagged for a human decision.

**Decision (2026-11-24): keep both behaviours exactly as they are in Phase 1.** The MVI/UDF refactor must reproduce them. Any fix happens
as a separate, explicitly reviewed change after the Phase 1 refactor is merged.

Baseline: `b18e513451` (`migration/ms-1440-pilot-phase-1`). Behaviour below comes from reading the code at that commit. It has not been
reproduced on a device.

| ID   | Screen(s)                                                       | Today                                                       | Phase 1                                               | After Phase 1                                   |
|------|-----------------------------------------------------------------|-------------------------------------------------------------|-------------------------------------------------------|-------------------------------------------------|
| KI-1 | `SyncInfoFragment` (main card, standalone screen, logout panel) | Re-login navigation is replayed to every newly created view | Keep; deliberate deviation from `viewmodel-udf.md` §5 | Make it a one-time `UiEffect`                   |
| KI-2 | `SettingsFragment`                                              | "Last updated" summary is lost when the view is re-created  | Keep; the standard §5 mapping already preserves it    | Move it into `UiState`, formatted when rendered |
| KI-3 | `SettingsFragment`                                              | App relaunch after a language change does not wait for the language to be saved | Keep the Fragment sequence; refresh runs in `launchWork` | Relaunch on an effect sent after the save |
| KI-4 | `SyncInfoFragment` (all hosts; duplicates only on the logout panel) | Each stream restart re-runs the initial-sync decision and adds another image auto-start collector | Keep; same 5 s stop timeout and `onStart` hooks | Start the image collector once; decide whether the re-check is intended |
| KI-5 | `RequestLoginFragment` (**unconfirmed**)                        | Logout-reason dialog may re-appear after returning from troubleshooting | No change (Fragment not refactored) | Confirm on a device, then decide "once per logout" |
| KI-6 | `MainFragment`                                                  | A failing configuration read crashes the app and skips the root check | Keep; the base-class guard catches only cancellation | Run the root check independently; log the config failure |
| KI-7 | `FingerSelectionFragment`                                       | An unknown finger type in the configuration crashes the list | Keep; unknown entries still reach the adapter | Skip unknown entries and log them |

KI-3 to KI-7 were found while planning the Phase 1 refactor (`feature-dashboard.p1-architecture.md`, and audit proposals M-5 and FS-4)
and added by human decision on 2026-11-24. All five are kept as they are in Phase 1, like KI-1 and KI-2.

---

## KI-1: re-login navigation is replayed when the sync-info view is re-created

Audit references: R-4 (b), §6.5, proposals S-5 and S-12.

### Current behaviour

- `SyncInfoViewModel.requestNavigationToLogin()` posts a `LoginParams` to `_loginNavigationEventLiveData`
  (`settings/syncinfo/SyncInfoViewModel.kt:54-56`, `:173-182`). It is a plain `MutableLiveData`, **not** a `LiveDataEvent`, and it is
  never cleared.
- `SyncInfoFragment.observeUI()` observes it with `viewLifecycleOwner` and calls
  `findNavController().navigate(graph_login, loginParams.toBundle())` (`settings/syncinfo/SyncInfoFragment.kt:146-148`).
- The ViewModel is fragment-scoped (`by viewModels()`, `SyncInfoFragment.kt:37`), so it survives while the Fragment is on the back stack
  and through configuration changes. Only the view is destroyed.
- LiveData delivers the latest value to every **new** observer. Each new `viewLifecycleOwner` is a new observer, so the last `LoginParams`
  is delivered again every time the view is re-created. An observer whose view only stops and starts again (the app goes to the
  background and back) does **not** get it again, because LiveData tracks the version each observer has already seen.
- LiveData does not deduplicate: pressing the button twice delivers twice, even when the `LoginParams` are equal.

### Risk

**Today, from reading the code:**

- **Return from login reopens login.** The user opens login from the sync-info view, then comes back with back or through
  `finishWithResult`.
  The view is re-created and the stale `LoginParams` navigates to `graph_login` again. `handleResult(LoginContract.DESTINATION)` is
  registered in the same `onViewCreated`, so a successful result also triggers `forceEventSync()`, but it does not stop the replay.
- **Re-creation reopens login.** Any later re-creation of the view (returning from Module selection, Settings, About, or after a
  configuration change) navigates to login again for as long as this Fragment instance lives.
- **All three hosts are affected.** The main-screen card, the standalone `syncInfoFragment` destination, and the panel inside
  `LogoutSyncFragment`.

This looks like a latent bug, but it is kept by decision. Confirm it on a device before scheduling the fix.

**During the refactor:**

- **The standard mapping silently fixes it.** `viewmodel-udf.md` §5 maps `MutableLiveData<NavParams>` to `UiEffect.Navigate(params)`.
  Following it would quietly change the behaviour. Because Phase 1 must keep current behaviour, this screen deviates from the map, and the
  deviation must be recorded in the handoff.
- **Plain state rendering over-navigates.** Rendering `loginParams` straight from `UiState` inside the standard
  `repeatOnLifecycle(STARTED)` collector would navigate on **every** collection restart (each background/foreground). It would also
  navigate on every unrelated state emission. That is a worse regression than either the old or the new behaviour.
- **Deduplicating by value drops presses.** Using `distinctUntilChanged()` on the `LoginParams` value drops a second press with equal
  params, which today navigates.

### Phase 1 approach (keep as-is)

- **ViewModel:** keep the request as state that is never cleared. It carries a sequence number so equal params are still distinct
  requests:
    - `data class LoginRequest(val seq: Int, val params: LoginParams)`, with `UiState.loginRequest: LoginRequest? = null`.
    - `UiAction.RequestLogin` builds the params exactly as today: `projectId = authStore.signedInProjectId` and
      `userId = authStore.signedInUserId ?: recentUserActivityManager.getRecentUserActivity().lastUserUsed`. It also increments `seq`.
- **Fragment:** use a dedicated collector whose deduplication lives as long as the **view**, not one STARTED period:
  ```kotlin
  viewLifecycleOwner.lifecycleScope.launch {
      viewModel.uiState
          .mapNotNull { it.loginRequest }
          .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
          .distinctUntilChangedBy { it.seq }
          .collect { findNavController().navigate(graph_login, it.params.toBundle()) }
  }
  ```
    - `distinctUntilChangedBy` sits downstream of `flowWithLifecycle`, so it is not reset when the app goes to the background and returns.
      No re-navigation on a stop/start, matching LiveData's per-observer version tracking.
    - A new view starts a new collector, so the latest request is delivered once to that view. This keeps today's replay.
    - A new press increments `seq`, so the user navigates on every press, as today.
- **Mark the deviation:** add a `// Parity with legacy LiveData replay, see feature-dashboard.p1-known-issues.md KI-1` comment in both
  places, and record it in the handoff as a deviation from `viewmodel-udf.md` §5 and §6.
- **Keep `navigate`:** the call stays `findNavController().navigate(...)`, not `navigateSafely`, because changing it changes the behaviour
  too.
- **Tests that pin it.** S-5 is rewritten to pin the current behaviour; this replaces the audit's "delivers once" wording:
    - **ViewModel:** `RequestLogin` sets `loginRequest` with the expected params and increments `seq`. Two presses with equal params produce
      two distinct `seq` values. Nothing clears `loginRequest`.
    - **Fragment (S-12 plus the parity cases):**
        - one press navigates once;
        - a stop/start of the same view does not navigate again;
        - re-creating the view navigates again with the same params;
        - a second press navigates again.

### Fix after Phase 1 (separate change)

- **Make it an effect:** replace `loginRequest` with `UiEffect.NavigateToLogin(params)` sent through `emitEffect`, as the
  `viewmodel-udf.md` §5 map specifies. Delete the dedicated collector and the `seq` wrapper. Effects are delivered once and stay buffered
  while the view is stopped, so login opens once per press and is never replayed.
- **Process death:** no `SavedStateHandle` persistence is needed. The re-login request is user-initiated and safe to lose on process death.
- **Navigation call:** consider switching to `navigateSafely(...)` in the same change.
- **Tests:** flip the parity tests to "re-creating the view does not navigate" and "each press navigates exactly once".
- **Before merging:** check on a device that returning from login no longer reopens it. Run the related E2E cases from the audit §9.

---

## KI-2: the Settings "last updated" summary is lost when the view is re-created

Audit references: R-4 (c), §6.6, proposal ST-9.

### Current behaviour

- `SettingsViewModel` sends the "since last updated" text as a `LiveDataEventWithContent<String>`. It sends it once from `load()`, which
  runs only in `init` (`settings/SettingsViewModel.kt:46-48`, `:54-56`, `:78`), and again after each `refreshConfiguration()` emission
  in `scheduleConfigUpdate()` (`:89-98`).
- `SettingsFragment` consumes it with `LiveDataEventWithContentObserver`. The observer sets the update-config preference summary to
  `dashboard_preference_summary_update_config_last_updated`, "Refresh device and project configuration\nLast updated: %s"
  (`settings/SettingsFragment.kt:82-90`).
- The ViewModel is fragment-scoped (`by viewModels()`, `SettingsFragment.kt:31`). When the user opens Finger selection, Sync info or
  About from Settings and comes back, the view is re-created, but the event has already been consumed and `load()` does not run again.
  The summary falls back to the XML default, `dashboard_preference_summary_update_config` ("Refresh device and project configuration"),
  from `res/xml/preference_general.xml:38`.

### Risk

- **Today:** a cosmetic inconsistency. "Last updated" shows on the first visit only and disappears after any round trip through a
  sub-screen, until the user taps update again. No data or flow is affected.
- **During the refactor:**
    - The standard `viewmodel-udf.md` §5 mapping (`LiveDataEventWithContent` → `UiEffect`) **keeps** the current behaviour: the
      effect is buffered until the first collector, delivered once, and not replayed to a re-created view.
    - The regression to watch is the opposite one: well-meaning "fixes" in Phase 1, such as putting the text into `UiState` or reloading in
      an `onViewCreated` action, would change visible behaviour without a decision.
    - The effect is sent during `init`, before the Fragment collects, so it depends on the base class's buffered channel. A
      `MutableSharedFlow` would drop it, and the summary would never show.

### Phase 1 approach (keep as-is)

- **Effect:** model it as `UiEffect.ShowConfigLastUpdated(text: String)`, sent with `emitEffect(...)` from the initial load and after each
  configuration refresh emission. Send it in the same order relative to the `configUpdated` effect as today: the toast effect first, then
  the summary.
- **Fragment:** the Fragment sets the preference summary from the effect only. No summary field in `UiState`, and no reload when the view
  is re-created.
- **Tests that pin it.** This is ST-9, now final:
    - **ViewModel:** the initial load emits `ShowConfigLastUpdated` with `configSyncCache.sinceLastUpdateTime()`. Each refresh emission
      emits the config-updated effect and then `ShowConfigLastUpdated`. ST-10 still applies: a refresh that emits nothing produces neither.
    - **Fragment:**
        - the first render shows the last-updated text;
        - after re-creating the view (navigate to About and back) the summary is the XML default;
        - after an update completes, the last-updated text is shown again.

### Fix after Phase 1 (separate change)

- **Store the time, not the text:** move the value into `UiState`. Store the last-update **timestamp**, not the pre-formatted relative
  string, so a re-created view does not show a stale "x minutes ago". Format it when rendering, or refresh it on a `ScreenStarted` action.
- **Render from state:** the Fragment renders the summary from state on every render. Remove `ShowConfigLastUpdated`.
- **Tests:** change the parity test to "after re-creating the view, the last-updated text is still shown and reflects the current time".
- **Phase 2:** the Compose Settings screen will need this value as state anyway, so the fix can also land as part of Phase 2 for
  Settings.

---

## KI-3: the app relaunches before the new language is saved

Handoff references: AP-11, DV-3; audit R-5, proposals ST-3 and ST-6.

### Current behaviour

- `SettingsFragment.handleLanguageChange` calls `viewModel.updateLanguagePreference(code)`, writes the language summary, then relaunches
  (`settings/SettingsFragment.kt:192-196`). The relaunch is `finishAffinity()`, `startActivity(DashboardActivity)` and
  `overridePendingTransition(0, 0)` (`:198-202`).
- `updateLanguagePreference` only launches a coroutine on `viewModelScope` (`settings/SettingsViewModel.kt:58-73`): it writes the device
  configuration and tracks `DeviceConfigurationUpdatedEvent` inside the transform, then posts the language and logs.
- Nothing makes the relaunch wait. Finishing the activity clears the Fragment's ViewModel, which cancels `viewModelScope`.

### Risk

- **Today (from reading the code, not reproduced):** if the write is slower than the activity teardown, the language change and its event
  can be lost, or the new activity can start in the old language.
- **During the refactor:** the save must still start immediately. If `LanguageSelected` were queued behind a long action (the
  configuration refresh), it might never run before the ViewModel is cleared. Moving the relaunch behind the save would change timing
  without a decision.

### Phase 1 approach (keep as-is)

- **Fragment:** same sequence: `onAction(LanguageSelected(code))`, write the summary for the selected code, relaunch.
- **ViewModel:** `LanguageSelected` is handled in `handleAction` in today's order (write + event, then `languageCode` state, then
  `Simber.i`). The configuration refresh runs in `launchWork`, so the queue is idle when the language is selected.
- **Mark it:** `// Parity with legacy relaunch timing, see feature-dashboard.p1-known-issues.md KI-3` in the Fragment.
- **Tests that pin it:** ST-3 (relaunch and summary), ST-6 (event tracked before the language output changes), N-4 (selection is not
  blocked by an in-flight refresh).

### Fix after Phase 1 (separate change)

- **Relaunch after the save:** the ViewModel emits `RelaunchApp` once the write has finished; the Fragment relaunches on that effect.
  Remove the immediate summary write.
- **Failure:** decide what happens if the write fails (no relaunch plus an error, or relaunch anyway).
- **Tests:** "relaunch happens only after the configuration write completes"; check on a device that the new language is applied.

---

## KI-4: each sync-info stream restart repeats the start-up checks

Handoff references: AP-3, DV-6, test N-3; audit §6.5.

### Current behaviour

- `syncInfoLiveData` (`settings/syncinfo/SyncInfoViewModel.kt:83-139`) wraps the data-layer flow in
  `onStart { startInitialSyncIfRequired(); syncImagesAfterEventsWhenRequired() }` (`:85-88`), shares it with `WhileSubscribed()`
  (`:88-92`) and exposes it with `asLiveData(viewModelScope.coroutineContext)` (`:138`), whose default stop timeout is 5 s.
- After 5 s or more without an active observer (app in background, host view stopped), the upstream stops; the next observer restarts it
  and both `onStart` hooks run again.
- `startInitialSyncIfRequired` (`:192-212`) re-decides the initial sync; in pre-logout mode it always restarts event sync.
- `syncImagesAfterEventsWhenRequired` (`:214-227`) launches a new, never-ending collector each time. In pre-logout mode every collector
  issues `OneTime.Images.start()` on its first value if events are already complete, and on every later completion transition.

### Risk

- **Today (not reproduced):** on the logout screen, every return from the background after 5 s restarts event sync and adds one more image
  auto-start collector, so `Images.start()` is issued several times per completion. On the main card the initial-sync check simply runs
  again on return, which may be intended.
- **During the refactor:** changing the stop timeout, or moving the hooks to `init`, changes when the checks run.

### Phase 1 approach (keep as-is)

- **ViewModel:** two-stage `stateIn` (outer `WhileSubscribed(5_000)`, inner `WhileSubscribed()`), so the total stop delay stays 5 s. The
  `onStart` hooks start through `launchWork` on each restart, as today.
- **Mark it:** `// Parity with legacy restart behaviour, see feature-dashboard.p1-known-issues.md KI-4` at the hooks.
- **Tests that pin it:** N-3 (a collector gap of 5 s or more re-runs the initial-sync decision and re-delivers the last state), S-1,
  S-16 and the existing initial-sync tests.

### Fix after Phase 1 (separate change)

- **Image auto-start:** start the pre-logout image collector once per ViewModel (or keep its `Job` and skip if active).
- **Initial sync:** decide whether re-checking on return is intended per host, and pin the decision.
- **Tests:** "repeated restarts issue `Images.start()` once per completion transition".

---

## KI-5: the logout-reason dialog may re-appear after returning from troubleshooting (unconfirmed)

Handoff references: D-9, BR-9, parity item PC-7; audit §6.9.

### Current behaviour

- `RequestLoginFragment.onViewCreated` sets `wasLogoutReasonDisplayed = savedInstanceState?.getBoolean(KEY_WAS_LOGOUT_REASON_DISPLAYED)
  ?: false` (`requestlogin/RequestLoginFragment.kt:48`) and shows the dialog when a reason exists and the flag is false (`:52`). The flag
  is saved in `onSaveInstanceState` (`:61-64`).
- Ten logo taps open troubleshooting (`:54-58`). Coming back re-creates the view from the back stack. If `onViewCreated` then receives
  `savedInstanceState == null`, the flag resets and the dialog is shown again.
- **Unconfirmed:** whether saved state is passed on back-stack view re-creation depends on the AndroidX Fragment version. Not reproduced.

### Risk

- **Today:** cosmetic; the project-ending message may be shown twice.
- **During the refactor:** none in Phase 1 (the Fragment is not changed). A later move of the flag into a ViewModel or `SavedStateHandle`
  would change this behaviour either way without anyone noticing.

### Phase 1 approach (keep as-is)

- **No production change.** RL-1, RL-2 and RL-4 pin the dialog on first show and across `recreate()`.
- **No unit test for the back-stack case:** the `TestNavHostController` setup does not re-create Fragment views. Confirm it on a device
  (audit §9 flow 18) before Phase 2 planning.

### Fix after Phase 1 (separate change)

- **Confirm first**, then decide the intended rule (most likely "once per logout").
- **Implement it** with the flag in a ViewModel `SavedStateHandle`, and add a test that navigates to troubleshooting and back.

---

## KI-6: a failing configuration read crashes the dashboard and skips the root check

Audit references: §6.2, proposal M-5. Handoff references: PC-13, D-1.

### Current behaviour

- `MainViewModel.load()` launches on `viewModelScope` and calls `configRepository.getProjectConfiguration()` first, then
  `checkIfDeviceIsSafe()` (`main/MainViewModel.kt:34-37`). There is no error handling around the configuration read.
- `checkIfDeviceIsSafe()` catches only `RootedDeviceException` (`:39-44`).
- If the configuration read throws (realistically a corrupt or undecryptable local DataStore; `ConfigRepositoryImpl` already catches
  failures to refresh from the network), the exception escapes `viewModelScope.launch`, the app crashes, and the root check never runs.

### Risk

- **Today:** the security gate depends on the configuration load succeeding. A crash instead of a rooted-device overlay.
- **During the refactor:** catching the exception, reordering the two calls, or running them in parallel would change behaviour without a
  decision. The base-class guard (`mvi-base-classes.md` §3.3) catches only `CancellationException`, so it does not change this.

### Phase 1 approach (keep as-is)

- **ViewModel:** `MainUiAction.Load` keeps the order (consent read, then root check) and adds no error handling. The exception propagates
  out of the action loop to `viewModelScope`, as it escapes `launch` today.
- **Tests that pin it (M-5):** when `getProjectConfiguration()` throws, `checkIfDeviceIsRooted()` is never called,
  `isRootedDeviceDetected` stays `false`, and the exception is not swallowed (it surfaces as an uncaught coroutine exception from the
  ViewModel's creation in the test). Written before the refactor and kept after it.

### Fix after Phase 1 (separate change)

- **Decouple:** run the root check independently of the configuration load, so a configuration failure can never skip it.
- **Log:** catch the configuration failure, log it through `Simber`, and decide what the privacy-notice item shows without a configuration.
- **Tests:** flip M-5 to "configuration read throws → root check still runs and no crash".

---

## KI-7: an unknown finger type in the configuration crashes Finger selection

Audit references: §6.8, proposal FS-4. Handoff references: PC-73.

### Current behaviour

- An unrecognised proto finger maps to `TemplateIdentifier.NONE`
  (`infra/config-store/src/main/java/com/simprints/infra/config/store/local/models/Finger.kt:20`).
- `FingerSelectionViewModel` copies the configured fingers into sections without filtering (`settings/fingerselection/
  FingerSelectionViewModel.kt:21-48`).
- `TemplateIdentifier.toString(context)` throws `IllegalArgumentException("Incorrect sample identifier")` for `NONE`
  (`settings/fingerselection/FingerSelectionItemAdapter.kt:104`), called from the item view holder's `bind` (`:85-88`).
- Result: Finger selection crashes while drawing its list if the configuration contains a finger type this app version does not know.

### Risk

- **Today:** a crash on a settings screen after a backend adds a new finger type.
- **During the refactor:** the immutable aggregation could start filtering `NONE` "by accident", which would hide the crash without a
  decision.

### Phase 1 approach (keep as-is)

- **ViewModel:** the aggregation keeps every configured entry, including `NONE`.
- **Adapter:** `toString(context)` unchanged.
- **Tests that pin it (FS-4):** the ViewModel keeps a `NONE` entry in its section; `TemplateIdentifier.NONE.toString(context)` throws
  `IllegalArgumentException`.

### Fix after Phase 1 (separate change)

- **Skip and log:** drop unknown entries when building the sections and log them through `Simber`; make `toString` total or unreachable for
  `NONE`.
- **Tests:** flip FS-4 to "unknown entries are skipped and the list renders".
