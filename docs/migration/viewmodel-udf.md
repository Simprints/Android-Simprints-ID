# ViewModel Architecture — MVI with Kotlin Flow

Reference document for the **architecture migration**: standardising ViewModels on a
single **MVI-style UDF contract** and replacing scattered `LiveData`, `LiveDataEvent`,
`LiveDataEventWithContent`, and ad-hoc navigation LiveData patterns.

This phase applies **while the UI is still XML/ViewBinding**. 
Compose screen implementation and Compose UI testing are covered separately in [compose-ui.md](compose-ui.md).
Module/orchestrator compatibility rules are in [module-contracts.md](module-contracts.md).

---

## 1 — MVI contract

This keeps all user/system actions explicit (`UiAction`), all render state immutable (`UiState`),
and all one-off side effects (`UiEffect`) separated from state.

---

## 2 — Define `UiState`, `UiAction`, `UiEffect` per screen

Each screen gets three co-located contracts in a single `MyScreenMVI` file:

```kotlin
internal data class MyScreenUiState(
    val isLoading: Boolean = false,
    val projectConfiguration: ProjectConfigurationUi? = null,
    val error: ErrorUi? = null,
) {
    internal data class ProjectConfigurationUi(
        val projectId: String,
        val projectName: String,
        val language: String,
    )

    internal data class ErrorUi(
        val message: String,
        val isRetryable: Boolean,
    )
}

internal sealed interface MyScreenUiAction {
    data object InitialLoad : MyScreenUiAction
    data object RetryClicked : MyScreenUiAction
    data object ContinueClicked : MyScreenUiAction
}

internal sealed interface MyScreenUiEffect {
    data object NavigateToLogin : MyScreenUiEffect
    data class ShowErrorSnackbar(val message: String) : MyScreenUiEffect
    data class NavigateToConsent(val params: ConsentParams) : MyScreenUiEffect
}
```

**Rules:**

- `UiState` is always an immutable data class with defaults.
- `UiAction` is the only entry point from UI into ViewModel.
- `UiEffect` contains one-time side effects only; never put render state in effects.
- Keep MVI contracts `internal` unless part of a module public API.

---

## 3 — Base Kotlin Flow MVI ViewModel (recommended default)

Use a small base class to remove boilerplate and enforce the same flow in every module.

```kotlin
internal abstract class BaseMviViewModel<UiState : Any, UiAction : Any, UiEffect : Any>(
    initialState: UiState,
) : ViewModel() {

    open val uiState: StateFlow<UiState>
        field = MutableStateFlow(initialState)

    // Default effect stream: no replay, buffered, supports Compose collectors.
    open val uiEffect: Flow<UiEffect>
        field = MutableSharedFlow<UiEffect>(replay = 0, extraBufferCapacity = 1)

    open fun onIntent(intent: UiAction) = viewModelScope.launch { handleIntent(intent) }
    
    protected abstract suspend fun handleIntent(intent: UiAction)

    protected fun reduce(reducer: (UiState) -> UiState) {
        uiState.update(reducer)
    }

    protected suspend fun emitEffect(effect: UiEffect) {
        uiEffect.tryEmit(effect)
    }
}
```

### Effect transport choice

- **Default:** `MutableSharedFlow` (`replay = 0`) for Compose-first UIs.
- **Use `Channel`** only when strict single-consumer FIFO queue semantics are required.

```kotlin
private val _uiEffect = Channel<MyScreenUiEffect>(capacity = Channel.BUFFERED)
val uiEffect: Flow<MyScreenUiEffect> = _uiEffect.receiveAsFlow()
```

---

## 4 — Screen ViewModel implementation pattern

This sample reflects a common Simprints flow where the screen bootstraps by loading
project configuration from local storage before allowing the user to continue.

```kotlin
@HiltViewModel
internal class MyScreenViewModel @Inject constructor(
    private val projectConfigurationStore: ProjectConfigurationStore,
    private val savedStateHandle: SavedStateHandle,
) : BaseMviViewModel<MyScreenUiState, MyScreenUiAction, MyScreenUiEffect>(
    initialState = MyScreenUiState(
        projectConfiguration = savedStateHandle.get<String>("project_id")?.let { projectId ->
            MyScreenUiState.ProjectConfigurationUi(
                projectId = projectId,
            )
        },
    ),
) {

    init {
        if (uiState.value.projectConfiguration == null) {
            onIntent(MyScreenUiAction.InitialLoad)
        }
    }

    override suspend fun handleIntent(intent: MyScreenUiAction) {
        when (intent) {
            MyScreenUiAction.InitialLoad,
            MyScreenUiAction.RetryClicked -> loadProjectConfiguration()
            MyScreenUiAction.ContinueClicked -> {
                val config = uiState.value.projectConfiguration
                if (config == null) {
                    emitEffect(MyScreenUiEffect.ShowErrorSnackbar("Project configuration unavailable"))
                    return
                }
                emitEffect(
                    MyScreenUiEffect.NavigateToConsent(
                        ConsentContract.getParams(projectId = config.projectId),
                    ),
                )
            }
        }
    }

    private suspend fun loadProjectConfiguration() {
        reduce { it.copy(isLoading = true, error = null) }

        val configuration = projectConfigurationStore.getProjectConfiguration()
        if (configuration != null) {
            savedStateHandle["project_id"] = configuration.projectId
            reduce {
                it.copy(
                    isLoading = false,
                    projectConfiguration = MyScreenUiState.ProjectConfigurationUi(
                        projectId = configuration.projectId,
                    ),
                )
            }
        } else {
            Simber.i("Project configuration not found in local storage")
            reduce {
                it.copy(
                    isLoading = false,
                    error = MyScreenUiState.ErrorUi(
                        message = "Unable to load project configuration",
                        isRetryable = true,
                    ),
                )
            }
            emitEffect(MyScreenUiEffect.ShowErrorSnackbar("Unable to load project configuration"))
        }
    }
}
```

**Key rules:**

- Keep `MutableStateFlow` and `MutableSharedFlow`/`Channel` private.
- Update state via reducers (`update`/`reduce`), not direct mutable UI fields.
- If values are persisted in `SavedStateHandle`, restore them in `initialState`.
- Do not expose mutable collections in `UiState`; expose immutable snapshots.
- Keep logging through `Simber`.

---

## 5 — Migration map from existing patterns

| Current pattern                                          | Replace with                                                                   |
|----------------------------------------------------------|--------------------------------------------------------------------------------|
| `MutableLiveData<T>` + backing field                     | `private val _uiState = MutableStateFlow(UiState())`                           |
| Public `LiveData<T>`                                     | `val uiState: StateFlow<UiState>`                                              |
| `LiveDataEvent` / `LiveDataEventWithContent<T>`          | `UiEffect` on `MutableSharedFlow(replay = 0)` (or `Channel`)                   |
| `MutableLiveData<NavParams>`                             | `UiEffect.Navigate(params)`                                                    |
| Fragment callback methods like `onRetry()` / `onClick()` | `onIntent(UiAction.RetryClicked)` / `onIntent(UiAction.X)`                     |
| Multiple LiveData fields                                 | One immutable `UiState`                                                        |
| `flow.asLiveData(...)`                                   | `flow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)` |
| Fragment `observe {}`                                    | Lifecycle-aware `uiState` collection (see section 6)                           |
| Fragment observation of one-time events                  | Lifecycle-aware `uiEffect` collection (see section 6)                          |

Once the screen moves to Compose, the last two rows become `collectAsStateWithLifecycle()` and
`LaunchedEffect(viewModel) { ... }` — see [phase2-compose-ui.md](compose-ui.md).

---

## 6 — Consuming state and effects from an XML Fragment

While the UI is still View-based, the Fragment renders from `UiState` only and translates `UiEffect`
into navigation and transient UI. No screen state is duplicated in the Fragment.

```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
            viewModel.uiState.collect { state -> render(state) }
        }
        launch {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    MyScreenUiEffect.NavigateToLogin -> findNavController().navigateSafely(...)
                    is MyScreenUiEffect.NavigateToConsent ->
                        findNavController().finishWithResult(this@MyFragment, ...)
                    is MyScreenUiEffect.ShowErrorSnackbar -> showSnackbar(effect.message)
                }
            }
        }
    }
}
```

**Rules:**

- Always collect with `repeatOnLifecycle(STARTED)` (or `flowWithLifecycle`) on
  `viewLifecycleOwner`, so collection stops with the view and effects are not lost or duplicated.
- User interactions call `viewModel.onIntent(UiAction.X)`; the Fragment makes no decisions.
- Navigation and result passing stay in the Fragment via `navigateSafely()` / `finishWithResult()`
  as required by [module-contracts.md](module-contracts.md).

---

## 7 — Migrating complex Flow ViewModels (`SyncInfoViewModel` style)

When a ViewModel already composes multiple flows, keep transformation in Flow and expose a single
hot `StateFlow<UiState>`:

```kotlin
val uiState: StateFlow<SyncInfoUiState> = mergedFlow
    .map { syncInfo -> syncInfo.toUiState() }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SyncInfoUiState(),
    )
```

`SharingStarted.WhileSubscribed(5_000)` is the default for screen state because it survives short
collector gaps (e.g., transient lifecycle changes) without keeping upstream alive indefinitely.

---

## 8 — Shared ViewModels (screen + workflow split)

For flows like biometric capture, use two ViewModels with strict boundaries:

- **Screen-scoped capture ViewModel**: camera permission/session, preview UI state, quality hints,
  capture button enablement, and one-time capture effects.
- **Shared workflow ViewModel** (activity or nav-graph scoped): template extraction, retries,
  progress, orchestration decisions, and final result state.

Do not inject one ViewModel into another. Bridge them in the host UI: the host observes the screen
ViewModel's effects and forwards them as actions to the workflow ViewModel (the Compose form of this
bridge is shown in [phase2-compose-ui.md](compose-ui.md)).

Scoping guidance:

- Prefer **nav-graph scoped shared ViewModels** when the workflow lifetime is tied to one graph.
- Use **activity scope** only when the workflow must span multiple graphs/screens.
- Keep resume-critical workflow values in `SavedStateHandle` on the shared workflow ViewModel.

---

## 9 — Testing the MVI contract

ViewModel and domain behavior are unit-tested with the project stack (JUnit 4, MockK, Truth, Turbine,
`TestCoroutineRule` from `:infra:test-tools`, `runTest { }`).

```kotlin
@Test
fun `initial load fetches project configuration`() = runTest {
        every { projectConfigurationStore.getProjectConfiguration() } returns ProjectConfiguration(
            projectId = "project-123",
        )

        viewModel.onIntent(MyScreenUiAction.InitialLoad)

        assertThat(viewModel.uiState.value.projectConfiguration?.projectId).isEqualTo("project-123")
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

@Test
fun `continue emits consent navigation effect`() = runTest {
    every { projectConfigurationStore.getProjectConfiguration() } returns ProjectConfiguration(
        projectId = "project-123",
    )

    viewModel.uiEffect.test {
        viewModel.onIntent(MyScreenUiAction.InitialLoad)
        viewModel.onIntent(MyScreenUiAction.ContinueClicked)
        assertThat(awaitItem()).isInstanceOf(MyScreenUiEffect.NavigateToConsent::class.java)
        cancelAndIgnoreRemainingEvents()
    }
}
```

For state progressions and effects in the same test, collect both streams:

```kotlin
@Test
fun `reducer updates state on login success`() = runTest {
        val repository = FakeUserRepository()
        val viewModel = LoginViewModel(repository)

        turbineScope {
            val stateTurbine = viewModel.uiState.testIn(backgroundScope)
            val effectsTurbine = viewModel.uiEffect.testIn(backgroundScope)

            // Initial state
            Truth.assertThat(stateTurbine.awaitItem()).isEqualTo(LoginUiState())

            // Trigger login
            viewModel.onIntent(LoginUiAction.SignInClicked("user@example.com", "password"))

            // Verify state progression
            Truth.assertThat(stateTurbine.awaitItem().isLoading).isTrue()
            Truth.assertThat(stateTurbine.awaitItem().isLoading).isFalse()

            // Verify effect
            Truth.assertThat(effectsTurbine.awaitItem())
                .isInstanceOf(LoginUiEffect.NavigateToHome::class.java)
        }
    }
```

Tests must collect the **public** `Flow<UiEffect>` regardless of the chosen effect transport.

### Migration checklist for ViewModel tests

- [ ] Replace `liveData.getOrAwaitValue()` with `uiState.value` assertions or Turbine collection
- [ ] Replace `LiveDataEvent` assertions with `Flow<UiEffect>` collection
- [ ] Remove old `LiveData` test helpers; use Turbine for Flow assertions
- [ ] Keep `TestCoroutineRule`; keep `InstantTaskExecutorRule` only while a test class still
      touches `LiveData`
- [ ] Cover each `UiAction` → state transition, validation rules, and error/retry paths
- [ ] Cover `SavedStateHandle` restoration where behavior depends on it
- [ ] Run the module test suite to ensure no timing issues

---

**Related references:**

- [module-contracts.md](module-contracts.md) — contract, navigation, and result guardrails that must
  not change during the refactor.
- [compose-ui.md](compose-ui.md) — Compose screen implementation and Compose UI tests.
