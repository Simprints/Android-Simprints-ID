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
