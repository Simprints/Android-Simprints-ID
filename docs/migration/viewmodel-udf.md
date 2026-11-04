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

Two delivery guarantees are part of the contract and are implemented once, in the base ViewModel (§3):

- **Actions are processed one at a time, in the order they were sent.** A second action never interleaves with the first one at a suspension
  point.
- **Effects are never lost while the UI is not collecting.** They are buffered until the screen is started again and delivered exactly once,
  like `LiveDataEvent` today.

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

## 3 — Base MVI ViewModel (required)

Every screen ViewModel extends this shared base class, which removes boilerplate and enforces the same action and effect guarantees in every
module. It lives in `:infra:ui-base` (package `com.simprints.infra.uibase.viewmodel`), which every feature module already depends on through
`ModuleFeatureConventionPlugin`.

The pilot (`:feature:dashboard`) adds it, with its unit tests, as an approved shared change in its Phase 1 handoff.
Later modules only use it; changes to the base class are separate PRs, never part of a module migration.

```kotlin
abstract class BaseMviViewModel<UiState : Any, UiAction : Any, UiEffect : Any>(
    initialState: UiState,
) : ViewModel() {

    open val uiState: StateFlow<UiState>
        field = MutableStateFlow(initialState)

    // Buffered until collected: effects sent while the UI is stopped are delivered when it starts again.
    private val effects = Channel<UiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<UiEffect> = effects.receiveAsFlow()

    // Single consumer: actions are handled sequentially, in the order they were sent.
    private val actions = Channel<UiAction>(Channel.UNLIMITED)

    init {
        viewModelScope.launch {
            for (action in actions) handleAction(action)
        }
    }

    fun onAction(action: UiAction) {
        actions.trySend(action)
    }

    protected abstract suspend fun handleAction(action: UiAction)

    protected fun reduce(reducer: (UiState) -> UiState) {
        uiState.update(reducer)
    }

    protected suspend fun emitEffect(effect: UiEffect) {
        effects.send(effect)
    }

    // Long-running or open-ended work (polling, sync observation, capture loops) that must not
    // hold up the action queue. Results come back as state updates or effects.
    protected fun launchWork(block: suspend CoroutineScope.() -> Unit): Job = viewModelScope.launch(block = block)
}
```

### Action processing

- `handleAction` runs for one action at a time. Rapid repeated input (double taps, retry during a load) is therefore handled
  deterministically: the second action sees the state produced by the first.
- Keep `handleAction` short. Work that takes noticeable time or never completes (observing a flow, polling, a capture loop) goes through
  `launchWork { }`, so later actions are not blocked; guard it with state (`isLoading`) if a second start must be ignored.
- Never launch coroutines directly from `onAction` or bypass the queue; every UI and system input goes through `onAction`.

### Effect transport

- Effects always use the base class channel (`Channel.BUFFERED` + `receiveAsFlow()`), sent with `emitEffect`. Do not use `MutableSharedFlow`
  for effects: it drops effects emitted while nothing is collecting (app in background, configuration change), which loses navigation and
  results.
- Each ViewModel has exactly one effect collector — its screen. A channel delivers every effect to one collector only.
- Effects do not survive process death. If a result or navigation must survive it, model it as state (for example a `pendingResult` field
  cleared by a `ResultDelivered` action) and persist it through `SavedStateHandle`.

---

## 4 — Screen ViewModel implementation pattern

This sample reflects a common Simprints flow where the screen bootstraps by loading project configuration from local storage before allowing
the user to continue.

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
            onAction(MyScreenUiAction.InitialLoad)
        }
    }

    override suspend fun handleAction(action: MyScreenUiAction) {
        when (action) {
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

- Keep `MutableStateFlow` and the effect/action channels private (the base class does this).
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
| `LiveDataEvent` / `LiveDataEventWithContent<T>`          | `UiEffect` sent with `emitEffect(...)` (buffered channel in the base class)    |
| `MutableLiveData<NavParams>`                             | `UiEffect.Navigate(params)`                                                    |
| Fragment callback methods like `onRetry()` / `onClick()` | `onAction(UiAction.RetryClicked)` / `onAction(UiAction.X)`                     |
| Multiple LiveData fields                                 | One immutable `UiState`                                                        |
| `flow.asLiveData(...)`                                   | `flow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)` |
| Fragment `observe {}`                                    | Lifecycle-aware `uiState` collection (see section 6)                           |
| Fragment observation of one-time events                  | Lifecycle-aware `uiEffect` collection (see section 6)                          |

Once the screen moves to Compose, the last two rows become `collectAsStateWithLifecycle()` and
`ObserveEffects(viewModel.uiEffect) { ... }` — see [compose-ui.md](compose-ui.md).

---

## 6 — Consuming state and effects from an XML Fragment

While the UI is still View-based, the Fragment renders from `UiState` only and translates `UiEffect` into navigation and transient UI. No
screen state is duplicated in the Fragment.

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
                        findNavController().finishWithResult(this@MyFragment, ...
                        )
                    is MyScreenUiEffect.ShowErrorSnackbar -> showSnackbar(effect.message)
                }
            }
        }
    }
}
```

**Rules:**

- Always collect with `repeatOnLifecycle(STARTED)` (or `flowWithLifecycle`) on `viewLifecycleOwner`, so collection stops with the view.
  Effects sent while the Fragment is stopped stay buffered and are delivered once it is started again.
- User interactions call `viewModel.onAction(UiAction.X)`; the Fragment makes no decisions.
- Navigation and result passing stay in the Fragment via `navigateSafely()` / `finishWithResult()` as required
  by [module-contracts.md](module-contracts.md).

---

## 7 — Migrating complex Flow ViewModels (`SyncInfoViewModel` style)

When a ViewModel already composes multiple flows, keep transformation in Flow and expose a single hot `StateFlow<UiState>`:

```kotlin
val uiState: StateFlow<SyncInfoUiState> = mergedFlow
    .map { syncInfo -> syncInfo.toUiState() }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SyncInfoUiState(),
    )
```

`SharingStarted.WhileSubscribed(5_000)` is the default for screen state because it survives short collector gaps (e.g., transient lifecycle
changes) without keeping upstream alive indefinitely.

---

## 8 — Shared ViewModels (screen + workflow split)

For flows like biometric capture, use two ViewModels with strict boundaries:

- **Screen-scoped capture ViewModel**: camera permission/session, preview UI state, quality hints, capture button enablement, and one-time
  capture effects.
- **Shared workflow ViewModel** (activity or nav-graph scoped): template extraction, retries, progress, orchestration decisions, and final
  result state.

Do not inject one ViewModel into another. Bridge them in the host UI: the host observes the screen ViewModel's effects and forwards them as
actions to the workflow ViewModel (the Compose form of this bridge is shown in [compose-ui.md](compose-ui.md)).

Scoping guidance:

- Prefer **nav-graph scoped shared ViewModels** when the workflow lifetime is tied to one graph.
- Use **activity scope** only when the workflow must span multiple graphs/screens.
- Keep resume-critical workflow values in `SavedStateHandle` on the shared workflow ViewModel.

---

## 9 — Testing the MVI contract

ViewModel and domain behavior are unit-tested with the project stack (JUnit 4, MockK, Truth, Turbine, `TestCoroutineRule` from
`:infra:test-tools`, `runTest { }`).

```kotlin
@Test
fun `initial load fetches project configuration`() = runTest {
        every { projectConfigurationStore.getProjectConfiguration() } returns ProjectConfiguration(
            projectId = "project-123",
        )

        viewModel.onAction(MyScreenUiAction.InitialLoad)

        assertThat(viewModel.uiState.value.projectConfiguration?.projectId).isEqualTo("project-123")
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

@Test
fun `continue emits consent navigation effect`() = runTest {
    every { projectConfigurationStore.getProjectConfiguration() } returns ProjectConfiguration(
        projectId = "project-123",
    )

    viewModel.uiEffect.test {
        viewModel.onAction(MyScreenUiAction.InitialLoad)
        viewModel.onAction(MyScreenUiAction.ContinueClicked)
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
            viewModel.onAction(LoginUiAction.SignInClicked("user@example.com", "password"))

            // Verify state progression
            Truth.assertThat(stateTurbine.awaitItem().isLoading).isTrue()
            Truth.assertThat(stateTurbine.awaitItem().isLoading).isFalse()

            // Verify effect
            Truth.assertThat(effectsTurbine.awaitItem())
                .isInstanceOf(LoginUiEffect.NavigateToHome::class.java)
        }
    }
```

Tests collect the **public** `Flow<UiEffect>`. With `TestCoroutineRule` (an `UnconfinedTestDispatcher` on `Main`), `onAction` is processed
eagerly, so assertions can follow it directly. Effects sent before the test starts collecting are buffered, not lost, so `uiEffect.test { }`
may start after the action.

### Migration checklist for ViewModel tests

- [ ] Replace `liveData.getOrAwaitValue()` with `uiState.value` assertions or Turbine collection
- [ ] Replace `LiveDataEvent` assertions with `Flow<UiEffect>` collection
- [ ] Remove old `LiveData` test helpers; use Turbine for Flow assertions
- [ ] Keep `TestCoroutineRule`; keep `InstantTaskExecutorRule` only while a test class still touches `LiveData`
- [ ] Cover each `UiAction` → state transition, validation rules, and error/retry paths
- [ ] Cover rapid repeated actions (double tap, retry during load) where order or duplication matters
- [ ] Cover effects emitted while nothing collects (they must still arrive once collection starts)
- [ ] Cover `SavedStateHandle` restoration where behavior depends on it
- [ ] Run the module test suite to ensure no timing issues

---

**Related references:**

- [module-contracts.md](module-contracts.md) — contract, navigation, and result guardrails that must not change during the refactor.
- [compose-ui.md](compose-ui.md) — Compose screen implementation and Compose UI tests.
