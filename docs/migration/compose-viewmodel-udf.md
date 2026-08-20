# ViewModel Modernisation — MVI with Kotlin Flow

Reference document for Phase 2 of the Compose migration. Standardises migrated ViewModels on a
single **MVI-style UDF contract** and replaces scattered `LiveData`, `LiveDataEvent`,
`LiveDataEventWithContent`, and ad-hoc navigation LiveData patterns.

---

## MVI contract

This keeps all user/system actions explicit (`UiAction`), all render state immutable (`UiState`),
and all one-off side effects (`UiEffect`) separated from state.

---

## 2a — Define `UiState`, `UiAction`, `UiEffect` per screen

Each screen gets three co-located contracts:

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
- Keep contracts `internal` unless part of a module public API.

---

## 2b — Base Kotlin Flow MVI ViewModel (recommended default)

Use a small base class to remove boilerplate and enforce the same flow in every module.

```kotlin
internal abstract class BaseMviViewModel<UiState : Any, UiAction : Any, UiEffect : Any>(
    initialState: UiState,
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Default effect stream: no replay, buffered, supports Compose collectors.
    private val _uiEffect = MutableSharedFlow<UiEffect>(
        replay = 0,
        extraBufferCapacity = 16,
    )
    val uiEffect: Flow<UiEffect> = _uiEffect.asSharedFlow()

    fun onIntent(intent: UiAction) {
        viewModelScope.launch {
            handleIntent(intent)
        }
    }

    protected abstract suspend fun handleIntent(intent: UiAction)

    protected fun reduce(reducer: (UiState) -> UiState) {
        _uiState.update(reducer)
    }

    protected suspend fun emitEffect(effect: UiEffect) {
        _uiEffect.emit(effect)
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

## 2c — Screen ViewModel implementation pattern

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

## 2d — Migration map from existing patterns

| Current pattern                                          | Replace with                                                                   |
|----------------------------------------------------------|--------------------------------------------------------------------------------|
| `MutableLiveData<T>` + backing field                     | `private val _uiState = MutableStateFlow(UiState())`                           |
| Public `LiveData<T>`                                     | `val uiState: StateFlow<UiState>`                                              |
| `LiveDataEvent` / `LiveDataEventWithContent<T>`          | `UiEffect` on `MutableSharedFlow(replay = 0)` (or `Channel`)                   |
| `MutableLiveData<NavParams>`                             | `UiEffect.Navigate(params)`                                                    |
| Fragment callback methods like `onRetry()` / `onClick()` | `onIntent(UiAction.RetryClicked)` / `onIntent(UiAction.X)`                     |
| Multiple LiveData fields                                 | One immutable `UiState`                                                        |
| `flow.asLiveData(...)`                                   | `flow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)` |
| Fragment `observe {}`                                    | Compose `collectAsStateWithLifecycle()`                                        |
| Fragment lifecycle collection for one-time events        | `LaunchedEffect(viewModel) { viewModel.uiEffect.collect { ... } }`             |

---

## 2e — Compose collection patterns

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

---

## 2f — Testing the MVI contract

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

Add Turbine where needed:

```kotlin
testImplementation(libs.turbine)
```

Keep `TestCoroutineRule`. Keep `InstantTaskExecutorRule` only while LiveData remains in that module.

---

## 2g — Migrating complex Flow ViewModels (`SyncInfoViewModel` style)

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

## 2h — Shared ViewModels (screen + workflow split)

For flows like biometric capture, use two ViewModels with strict boundaries:

- **Screen-scoped capture ViewModel**: camera permission/session, preview UI state, quality hints,
  capture button enablement, and one-time capture effects.
- **Shared workflow ViewModel** (activity or nav-graph scoped): template extraction, retries,
  progress, orchestration decisions, and final result state.

Do not inject one ViewModel into another. Bridge them in the host UI:

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

Scoping guidance:

- Prefer **nav-graph scoped shared ViewModels** when the workflow lifetime is tied to one graph.
- Use **activity scope** only when the workflow must span multiple graphs/screens.
- Keep resume-critical workflow values in `SavedStateHandle` on the shared workflow ViewModel.
