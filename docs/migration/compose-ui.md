# Compose Screen Implementation and UI Testing

Reference document for the **Compose UI phase (Phase 2)**: replacing XML/ViewBinding screens with
Compose while the ViewModel keeps the MVI/UDF contract established in
[viewmodel-udf.md](viewmodel-udf.md).

Theme tokens and component styling rules live in
[compose-theme.md](compose-theme.md). Contract, navigation, and result guardrails live
in [module-contracts.md](module-contracts.md).

**Prerequisite:** the screen's ViewModel already exposes `StateFlow<UiState>` and `Flow<UiEffect>`.

---

## 1 — Compose UI feature flag

Phase 2 ships Compose screens **next to** the legacy XML screens, behind a build-time flag, until the whole module is migrated and
validated. Only then is the legacy UI deleted.

The flag applies to Phase 2 only. The Phase 1 MVI/UDF refactor is never flagged: both UI variants render from the same ViewModel, `UiState`
and `UiEffect` contract.

### Flag definition

- `ModuleFeatureConventionPlugin` generates `BuildConfig.COMPOSE_UI_ENABLED` for every feature module.
- `release`: always `false`.
- `debug` and `staging`: `true` only when the Gradle property `simprints.compose.<module>` is `true`, where `<module>` is the module path
  without the leading colon and with colons as dashes. Example: `-Psimprints.compose.feature-dashboard=true`.
- **Off by default.** CI and regular builds exercise the legacy UI that release ships. Enable the flag locally (`-P...` or
  `~/.gradle/gradle.properties`) or in the QA/staging build job used for validation.
- The value is a compile-time constant, so in release the Compose branch is folded away and R8 strips the unreferenced Compose code. Compose
  dependencies are regular `implementation` dependencies.

The flag, the Compose convention plugin and `:infra:compose-common` are shared setup from
[compose-preparation.md](compose-preparation.md); module PRs never add them.

### Fragment shell with both variants

Compose screens are hosted by the existing Fragment destination so nav graph IDs, orchestrator dispatch, and result passing stay unchanged (
see `module-contracts.md`, Guardrails 5–7).

```kotlin
@AndroidEntryPoint
internal class MyFragment : Fragment(R.layout.fragment_my) {

    private val viewModel: MyScreenViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = if (BuildConfig.COMPOSE_UI_ENABLED) {
        // From :infra:compose-common: sets the ViewCompositionStrategy, SimTheme and testTagsAsResourceId.
        simComposeView {
            MyScreen(
                viewModel = viewModel,
                onComplete = ::finishWith,
                onNavigateToConsent = ::navigateToConsent,
            )
        }
    } else {
        super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!BuildConfig.COMPOSE_UI_ENABLED) {
            setupLegacyUi() // existing ViewBinding rendering and effect collection, unchanged
        }
    }

    // Shared by both variants, so navigation and result wiring exist exactly once.
    private fun finishWith(result: MyResult) {
        findNavController().finishWithResult(this, result)
    }

    private fun navigateToConsent(params: ConsentParams) {
        findNavController().navigateSafely(this, ...)
    }
}
```

**Rules:**

- Keep the destination entry in `graph_foo.xml` with the same `android:id`; the Fragment class may be renamed, the destination ID may not.
- Always host Compose through `simComposeView { }`; never hand-roll the `ComposeView`. It disposes the composition with the view, wraps
  content in `SimTheme` (at this boundary only, never inside individual screens), and enables `testTagsAsResourceId` for Appium.
- Do not call `applySystemBarInsets` on the Compose shell; Compose screens handle insets themselves (section 3).
- Navigation and results stay in the Fragment: the composable receives lambdas (`onComplete`, `onNavigateX`) and never calls the
  `NavController` itself. Both variants call the same Fragment navigation/result functions.
- Return results only through `finishWithResult(...)`; never `Activity.setResult()` and never via a shared ViewModel.
- One flag per module. Screens are migrated in separate PRs; a screen that has not been migrated yet has no branch and keeps rendering XML
  with the flag on.
- The legacy path is frozen: no changes beyond critical fixes, and every such fix is mirrored in the Compose path in the same PR.
- Neither branch contains logic beyond rendering and wiring; decisions stay in the ViewModel.

### Testing while both variants exist

- Compose route/content composables are tested directly with `createComposeRule()` (sections 7–10); these tests do not depend on the flag
  and run in CI.
- Existing legacy Fragment tests stay unchanged and keep running in CI, because the flag is off by default.
- Tests of the Fragment's Compose wiring (shell lambdas → `finishWithResult` / `navigateSafely`) start with
  `assumeTrue(BuildConfig.COMPOSE_UI_ENABLED)` and run when the flag is enabled.

### Legacy removal (module cleanup)

Once every screen in the module is migrated **and** validated (QA on a flag-enabled staging build, plus human sign-off in the module's Phase
2 handoff), a dedicated cleanup PR:

- makes the Compose path unconditional and removes every `COMPOSE_UI_ENABLED` branch in the module,
- deletes legacy layouts, ViewBinding usage, adapters, custom Views, the in-module XML navigation replaced by Navigation 3 (section 2), and
  legacy Fragment tests that only cover deleted UI,
- removes now-unused resources.

This PR is what ships the module's Compose UI to production.

---

## 2 — Navigation inside a module (Navigation 3)

Navigation **between** modules is untouched in Phase 2. Navigation **within** a module moves to Navigation 3.

**Stays on XML Navigation (module boundary):**

- The public `graph_foo.xml`, its entry destination `android:id`, and its action IDs.
- Any destination referenced by the orchestrator (`Contract.DESTINATION`, `BuildStepsUseCase`) or by another module's graph, `<include>` or
  action.
- Global/cross-module actions such as `action_global_refusalFragment` → exit form. The composable calls a shell lambda, and the Fragment
  performs the XML navigation.

**Moves to Navigation 3 (inside the module):**

- Screens reachable only from within the module: destinations of internal graphs (`graph_foo_internal.xml` hosted by a `NavHostFragment` in
  the entry/controller Fragment) and non-entry destinations in the module graph.
- They become entries of a single `NavDisplay` inside the entry Fragment's `ComposeView`.

```kotlin
@Serializable
internal data object ScanKey : NavKey
@Serializable
internal data class ConfirmKey(val credentialId: String) : NavKey

@Composable
internal fun ExternalCredentialNavHost(
    onComplete: (ExternalCredentialResult) -> Unit,
    onExitForm: () -> Unit,
) {
    val backStack = rememberNavBackStack(ScanKey)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<ScanKey> {
                ScanScreen(onScanned = { id -> backStack.add(ConfirmKey(id)) }, onExitForm = onExitForm)
            }
            entry<ConfirmKey> { key ->
                ConfirmScreen(credentialId = key.credentialId, onComplete = onComplete)
            }
        },
    )
}
```

**Rules:**

- Keys are `@Serializable`, `internal` to the module, and implement `NavKey`, so the back stack survives configuration changes and process
  death.
- Per-entry ViewModels use `hiltViewModel()` inside the entry, scoped by the ViewModel-store decorator. A workflow ViewModel shared by all
  entries stays scoped to the host Fragment, bridged as in section 5.
- When the back stack holds only its start entry, back handling falls through to the Fragment's existing behavior (exit form, cancel result,
  etc.); match the legacy behavior exactly.
- While the flag exists, the legacy internal XML graph stays in place for the flag-off path. It is deleted in the cleanup PR (section 1).
- The Phase 2 handoff lists every destination in the module and classifies it as **boundary** (XML) or **internal** (Navigation 3), with
  evidence for each boundary classification.

---

## 3 — Screen structure

Split each screen into a stateful route and a stateless content composable:

- **Route** (`MyScreen`): obtains the ViewModel with `hiltViewModel()`, collects state and effects, and maps effects to the host lambdas.
- **Content** (`MyScreenContent`): pure function of `UiState` plus callbacks; no ViewModel, no business logic, no navigation. This is the
  unit that previews and UI tests target.

Additional rules:

- Every composable accepts and applies a `modifier` parameter.
- Use `SimTheme` tokens for colors, typography, shapes, and spacing; never hardcode values.
- Lazy lists use stable `key`s, and `UiState` types stay immutable so recomposition stays cheap.
- Provide previews for the meaningful states (loading, content, empty, error).
- Add `BackHandler` only where the XML screen had custom back behavior.
- Apply `WindowInsets.safeDrawing` (or the specific inset) so content is never clipped or hidden by system bars or the IME; system bar
  styling is configured at the Activity level (see `compose-theme.md`).

### Test tags

Every actionable element and every label that tests or Appium rely on gets a test tag, built with the test tag tooling from
`:infra:compose-common` (screen prefix supplied by a `TestTagScope`):

| Component   | Format                      | Example                    |
|-------------|-----------------------------|----------------------------|
| Buttons     | `<screen>_btn_<action>`     | `login_btn_login`          |
| Inputs      | `<screen>_input_<field>`    | `login_input_password`     |
| List items  | `<screen>_item_<id/index>`  | `modules_item_<id>`        |
| Text labels | `<screen>_lbl_<descriptor>` | `consent_lbl_general_text` |

Keep the tags of each screen in one `internal object <Screen>Tags`, so tests and Appium share one contract. `simComposeView` exposes them as
resource IDs.

### Adaptive layout (required)

Every migrated screen must work in portrait and landscape and across compact, medium, and expanded window widths, even if the legacy XML
screen did not.

- Read the window size class in the route (`currentWindowAdaptiveInfo().windowSizeClass`) and pass the resulting layout decision to the
  content composable as a parameter, so previews and tests can drive every variant.
- Use Material 3 adaptive components and layouts rather than hardcoded breakpoints or orientation checks.
- Keep state across rotation: render state comes from the ViewModel, and transient UI state uses `rememberSaveable`.
- Provide previews for phone portrait, phone landscape, and tablet/expanded width.
- Add at least one UI test per screen for a non-default size (for example with `DeviceConfigurationOverride.ForcedSize`) asserting that key
  content and actions are visible and reachable.
- Orientation locks (e.g. `android:screenOrientation="portrait"` on `DashboardActivity`) stay for the legacy path. With the flag on, the
  host Activity requests `SCREEN_ORIENTATION_UNSPECIFIED` at runtime; the cleanup PR removes the manifest lock.

---

## 4 — Collecting state and effects in Compose

```kotlin
@Composable
internal fun MyScreen(
    viewModel: MyScreenViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToConsent: (ConsentParams) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ObserveEffects(viewModel.uiEffect) { effect ->
        when (effect) {
            MyScreenUiEffect.NavigateToLogin -> onNavigateToLogin()
            is MyScreenUiEffect.ShowErrorSnackbar -> {
                // trigger snackbar host state
            }
            is MyScreenUiEffect.NavigateToConsent -> {
                onNavigateToConsent(effect.params)
            }
        }
    }

    MyScreenContent(
        state = state,
        onRetry = { viewModel.onAction(MyScreenUiAction.RetryClicked) },
        onContinue = { viewModel.onAction(MyScreenUiAction.ContinueClicked) },
    )
}
```

Use `collectAsStateWithLifecycle()` for render state and `ObserveEffects` for effects. Never collect effects with a bare
`LaunchedEffect { flow.collect { } }`: it keeps collecting while the screen is stopped, so effects (navigation, results) are consumed in the
background instead of staying buffered until the screen is visible again.

`ObserveEffects` lives in `:infra:compose-common` and is the only allowed way to collect effects:

```kotlin
@Composable
fun <T> ObserveEffects(effects: Flow<T>, onEffect: suspend (T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnEffect by rememberUpdatedState(onEffect)
    LaunchedEffect(effects, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                effects.collect { currentOnEffect(it) }
            }
        }
    }
}
```

---

## 5 — Shared workflow ViewModels in Compose

For the screen + workflow ViewModel split described in `viewmodel-udf.md`, bridge the two
in the host composable instead of injecting one ViewModel into another:

```kotlin
@Composable
internal fun CaptureRoute(
    captureViewModel: CaptureViewModel = hiltViewModel(),
    workflowViewModel: CaptureWorkflowViewModel,
) {
    val captureState by captureViewModel.uiState.collectAsStateWithLifecycle()

    ObserveEffects(captureViewModel.uiEffect) { effect ->
        when (effect) {
            is CaptureUiEffect.CaptureCompleted -> {
                workflowViewModel.onAction(
                    CaptureWorkflowUiAction.ExtractTemplate(
                        imageBytes = effect.imageBytes,
                        metadata = effect.metadata,
                    ),
                )
            }
        }
    }

    CaptureScreen(
        state = captureState,
        onCapture = { captureViewModel.onAction(CaptureUiAction.CaptureClicked) },
    )
}
```

---

## 6 — What changes in UI testing

| Aspect                        | Legacy (View + LiveData)                                                | New (Compose + UDF)                                            |
|-------------------------------|-------------------------------------------------------------------------|----------------------------------------------------------------|
| **State observation**         | `LiveData.getOrAwaitValue()` (blocking)                                 | `collectAsStateWithLifecycle()` (reactive collection)          |
| **Effects (one-time events)** | `LiveDataEvent` wrapper + `getOrAwaitValue()`                           | `Flow<UiEffect>` + `ObserveEffects` collection                 |
| **Test helpers**              | `InstantTaskExecutorRule` + `getOrAwaitValue()` from `infra/test-tools` | None—use standard Compose `@Composable` testing + Turbine      |
| **Rendering**                 | Fragment layout inflation + assertions on View properties               | Compose `ComposeTestRule` + `printToLog()` + semantic matchers |

ViewModel-level unit tests (state transitions, effects, domain rules) stay as defined in
`viewmodel-udf.md` §9. This section covers rendering and interaction tests.

---

## 7 — Testing `UiState` rendering

Use **`collectAsStateWithLifecycle()`** to observe `StateFlow<UiState>` in tests:

```kotlin
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun `displays loading state initially`() {
    val viewModel = MyScreenViewModel(...)  // or inject via @HiltViewModel + testRule

    composeTestRule.setContent {
        SimTheme {
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            MyScreenContent(
                state = uiState,
                onRetry = { viewModel.onAction(MyScreenUiAction.RetryClicked) },
                onContinue = { viewModel.onAction(MyScreenUiAction.ContinueClicked) },
            )
        }
    }

    // Assert initial state
    composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()

    // Trigger action
    composeTestRule.onNodeWithContentDescription("Retry").performClick()

    // Wait for new state and assert
    composeTestRule.waitUntil(timeoutMillis = 5000) {
        composeTestRule.onAllNodesWithText("Success").fetchSemanticsNodes().isNotEmpty()
    }
}
```

---

## 8 — Testing `UiEffect` handling

Collect effects in the test the same way the route does, with **`ObserveEffects`**:

```kotlin
@Test
fun `navigates when login fails`() {
    val viewModel = LoginViewModel(...)
    val capturedEffects = mutableListOf<LoginUiEffect>()

    composeTestRule.setContent {
        SimTheme {
            ObserveEffects(viewModel.uiEffect) { effect ->
                capturedEffects.add(effect)
            }
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            LoginScreenContent(state = uiState, onAction = viewModel::onAction)
        }
    }

    // Trigger invalid login
    composeTestRule.onNodeWithContentDescription("Email").performTextInput("invalid@")
    composeTestRule.onNodeWithText("Sign In").performClick()

    // Assert effect was emitted
    composeTestRule.waitUntil(timeoutMillis = 5000) {
        capturedEffects.any { it is LoginUiEffect.ShowError }
    }
    Truth.assertThat(capturedEffects).hasSize(1)
}
```

---

## 9 — Key differences from legacy UI testing

1. **No `InstantTaskExecutorRule` needed** — `StateFlow` and effect `Flow`s are not tied to a `LiveData` executor.
2. **No `getOrAwaitValue()` helper** — state is immutable; just read `.value` synchronously after collection.
3. **Effect transport follows the UDF guide** — effects come from the base ViewModel's buffered channel (`viewmodel-udf.md` §3). Tests
   collect the public `Flow<UiEffect>`; there is exactly one collector per ViewModel.
4. **Compose test rule is required** — you must compose the screen to trigger recompositions and verify UI.
5. **Effects are collected with `ObserveEffects`** — it collects only while the lifecycle is at least `STARTED`, so effects sent while the
   screen is stopped stay buffered and arrive once it resumes.

---

## 10 — Compose UI test checklist per migrated screen

- [ ] Rendering asserted for each meaningful `UiState` (loading, content, empty, error)
- [ ] Each interaction dispatches the expected `UiAction`
- [ ] Validation feedback and enabled/disabled action states asserted
- [ ] Retry and recovery paths asserted
- [ ] Effects verified: navigation lambdas and result completion invoked with the right payload
- [ ] Customised back handling asserted
- [ ] Accessibility semantics present (labels for actionable elements, reachable nodes)
- [ ] `waitUntil { }` used for async state instead of sleeps
- [ ] Actionable elements and test-relevant labels tagged per the test tag convention
- [ ] Non-default window size covered (landscape or expanded width)
- [ ] Fragment-shell wiring tests guarded with `assumeTrue(BuildConfig.COMPOSE_UI_ENABLED)`
- [ ] `@get:Rule val composeTestRule = createComposeRule()` (or `createAndroidComposeRule`) in place

---

**Related references:**

- [viewmodel-udf.md](viewmodel-udf.md) — MVI contract and ViewModel/domain tests.
- [compose-theme.md](compose-theme.md) — `SimTheme`, tokens, edge-to-edge styling.
- [module-contracts.md](module-contracts.md) — orchestrator, navigation, and result guardrails.
