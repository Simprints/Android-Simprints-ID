# Compose Screen Implementation and UI Testing

Reference document for the **Compose UI phase (Phase 2)**: replacing XML/ViewBinding screens with
Compose while the ViewModel keeps the MVI/UDF contract established in
[phase1-viewmodel-mvi-udf.md](viewmodel-udf.md).

Theme tokens and component styling rules live in
[phase2-compose-theme.md](compose-theme.md). Contract, navigation, and result guardrails live
in [module-contracts.md](module-contracts.md).

**Prerequisite:** the screen's ViewModel already exposes `StateFlow<UiState>` and `Flow<UiEffect>`.

---

## 1 — Interop Fragment shell

Compose screens are hosted by the existing Fragment destination so nav graph IDs, orchestrator
dispatch, and result passing stay unchanged (see `module-contracts.md`, Guardrails 5–7).

```kotlin
internal class MyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            SimTheme {
                MyScreen(
                    onComplete = { result ->
                        findNavController().finishWithResult(this@MyFragment, result)
                    },
                    onNavigateToConsent = { params ->
                        findNavController().navigateSafely(this@MyFragment, ...)
                    },
                )
            }
        }
    }
}
```

**Rules:**

- Keep the destination entry in `graph_foo.xml` with the same `android:id`; the Fragment class may
  be renamed, the destination ID may not.
- Always set a `ViewCompositionStrategy` so the composition is disposed with the view.
- `SimTheme` wraps content at this boundary only — not inside individual screens.
- Navigation and results stay in the Fragment: the composable receives lambdas
  (`onComplete`, `onNavigateX`) and never calls the `NavController` itself.
- Return results only through `finishWithResult(...)`; never `Activity.setResult()` and never via a
  shared ViewModel.

---

## 2 — Screen structure

Split each screen into a stateful route and a stateless content composable:

- **Route** (`MyScreen`): obtains the ViewModel with `hiltViewModel()`, collects state and effects,
  and maps effects to the host lambdas.
- **Content** (`MyScreenContent`): pure function of `UiState` plus callbacks; no ViewModel, no
  business logic, no navigation. This is the unit that previews and UI tests target.

Additional rules:

- Every composable accepts and applies a `modifier` parameter.
- Use `SimTheme` tokens for colors, typography, shapes, and spacing; never hardcode values.
- Lazy lists use stable `key`s, and `UiState` types stay immutable so recomposition stays cheap.
- Provide previews for the meaningful states (loading, content, empty, error).
- Add `BackHandler` only where the XML screen had custom back behavior.
- Apply `WindowInsets.safeDrawing` (or the specific inset) so content is never clipped or hidden by
  system bars or the IME; system bar styling is configured at the Activity level
  (see `phase2-compose-theme.md`).

---

## 3 — Collecting state and effects in Compose

```kotlin
@Composable
internal fun MyScreen(
    viewModel: MyScreenViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToConsent: (ConsentParams) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
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
    }

    MyScreenContent(
        state = state,
        onRetry = { viewModel.onIntent(MyScreenUiAction.RetryClicked) },
        onContinue = { viewModel.onIntent(MyScreenUiAction.ContinueClicked) },
    )
}
```

Use `collectAsStateWithLifecycle()` for render state and `LaunchedEffect(viewModel)` for effects.
Never collect effects outside a lifecycle-aware effect API — that is how navigation gets missed or
duplicated.

---

## 4 — Shared workflow ViewModels in Compose

For the screen + workflow ViewModel split described in `phase1-viewmodel-mvi-udf.md`, bridge the two
in the host composable instead of injecting one ViewModel into another:

```kotlin
@Composable
internal fun CaptureRoute(
    captureViewModel: CaptureViewModel = hiltViewModel(),
    workflowViewModel: CaptureWorkflowViewModel,
) {
    val captureState by captureViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(captureViewModel) {
        captureViewModel.uiEffect.collect { effect ->
            when (effect) {
                is CaptureUiEffect.CaptureCompleted -> {
                    workflowViewModel.onIntent(
                        CaptureWorkflowUiAction.ExtractTemplate(
                            imageBytes = effect.imageBytes,
                            metadata = effect.metadata,
                        ),
                    )
                }
            }
        }
    }

    CaptureScreen(
        state = captureState,
        onCapture = { captureViewModel.onIntent(CaptureUiAction.CaptureClicked) },
    )
}
```

---

## 5 — What changes in UI testing

| Aspect                        | Legacy (View + LiveData)                                                | New (Compose + UDF)                                            |
|-------------------------------|-------------------------------------------------------------------------|----------------------------------------------------------------|
| **State observation**         | `LiveData.getOrAwaitValue()` (blocking)                                 | `collectAsStateWithLifecycle()` (reactive collection)          |
| **Effects (one-time events)** | `LiveDataEvent` wrapper + `getOrAwaitValue()`                           | `Flow<UiEffect>` + `LaunchedEffect` collection                 |
| **Test helpers**              | `InstantTaskExecutorRule` + `getOrAwaitValue()` from `infra/test-tools` | None—use standard Compose `@Composable` testing + Turbine      |
| **Rendering**                 | Fragment layout inflation + assertions on View properties               | Compose `ComposeTestRule` + `printToLog()` + semantic matchers |

ViewModel-level unit tests (state transitions, effects, domain rules) stay as defined in
`phase1-viewmodel-mvi-udf.md` §9. This section covers rendering and interaction tests.

---

## 6 — Testing `UiState` rendering

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
            MyScreen(uiState = uiState, onEvent = {})
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

## 7 — Testing `UiEffect` handling

Use **`LaunchedEffect`** to collect and verify effects:

```kotlin
@Test
fun `navigates when login fails`() {
    val viewModel = LoginViewModel(...)
    val capturedEffects = mutableListOf<LoginUiEffect>()

    composeTestRule.setContent {
        SimTheme {
            LaunchedEffect(Unit) {
                viewModel.uiEffect.collect { effect ->
                    capturedEffects.add(effect)
                }
            }
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            LoginScreen(uiState = uiState, onIntent = viewModel::onIntent)
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

## 8 — Key differences from legacy UI testing

1. **No `InstantTaskExecutorRule` needed** — `StateFlow` and effect `Flow`s are not tied to a
   `LiveData` executor.
2. **No `getOrAwaitValue()` helper** — state is immutable; just read `.value` synchronously after collection.
3. **Effect transport follows the UDF guide** — use `MutableSharedFlow(replay = 0)` by default.
   Use `Channel` only when strict single-consumer FIFO queue semantics are required. Tests must
   collect the public `Flow<UiEffect>` regardless of the selected transport.
4. **Compose test rule is required** — you must compose the screen to trigger recompositions and verify UI.
5. **Effects collection must happen in a `LaunchedEffect`** — this ensures the collector is lifecycle-aware and survives configuration
   changes during the test.

---

## 9 — Compose UI test checklist per migrated screen

- [ ] Rendering asserted for each meaningful `UiState` (loading, content, empty, error)
- [ ] Each interaction dispatches the expected `UiAction`
- [ ] Validation feedback and enabled/disabled action states asserted
- [ ] Retry and recovery paths asserted
- [ ] Effects verified: navigation lambdas and result completion invoked with the right payload
- [ ] Customised back handling asserted
- [ ] Accessibility semantics present (labels for actionable elements, reachable nodes)
- [ ] `waitUntil { }` used for async state instead of sleeps
- [ ] `@get:Rule val composeTestRule = createComposeRule()` (or `createAndroidComposeRule`) in place

---

**Related references:**

- [phase1-viewmodel-mvi-udf.md](viewmodel-udf.md) — MVI contract and ViewModel/domain tests.
- [phase2-compose-theme.md](compose-theme.md) — `SimTheme`, tokens, edge-to-edge styling.
- [module-contracts.md](module-contracts.md) — orchestrator, navigation, and result guardrails.
