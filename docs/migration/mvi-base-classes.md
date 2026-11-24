# MVI base classes — implementation specification

Source of truth for implementing the shared MVI base class and its test helpers. `viewmodel-udf.md` describes the contract every screen
follows; this document specifies the code that enforces it. When the two differ on implementation detail, this document wins;
`viewmodel-udf.md` §3 stays the conceptual summary.

- **Introduced by:** the Phase 1 pilot (`:feature:dashboard`, handoff `handoffs/feature-dashboard.p1-architecture.md`, step 1).
- **Applies to:** every module migrated in Phase 1. Modules only *use* these classes. Any change to them is a separate PR with its own
  tests, never part of a module migration (`viewmodel-udf.md` §3).
- **Decided:** 2026-11-24 (human review of the pilot handoff, decision on DV-4).

---

## 1 — Location and dependencies

| Item            | Value                                                                                                                                                                                                                                    |
|-----------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Module          | `:infra:ui-base` (every feature module already depends on it through `ModuleFeatureConventionPlugin`)                                                                                                                                    |
| Package         | `com.simprints.infra.uibase.viewmodel`                                                                                                                                                                                                   |
| Production file | `infra/ui-base/src/main/java/com/simprints/infra/uibase/viewmodel/BaseMviViewModel.kt`                                                                                                                                                   |
| Test file       | `infra/ui-base/src/test/java/com/simprints/infra/uibase/viewmodel/BaseMviViewModelTest.kt`                                                                                                                                               |
| Dependencies    | None new. `androidx.lifecycle:lifecycle-viewmodel-ktx` (`viewModelScope`) and coroutines are already `api` dependencies of `:infra:ui-base`; JUnit 4, MockK, Truth, Turbine and `kotlinx-coroutines-test` come from `:infra:test-tools`. |
| Compiler flags  | None new. Do not edit `build-logic` for this class; if a flag seems necessary, use the fallback in section 4.                                                                                                                            |

---

## 2 — Public API

```kotlin
package com.simprints.infra.uibase.viewmodel

abstract class BaseMviViewModel<UiState : Any, UiAction : Any, UiEffect : Any>(
    initialState: UiState,
) : ViewModel() {

    /** Current screen state. Open so stream-composed ViewModels can expose a derived StateFlow (section 3.1). */
    open val uiState: StateFlow<UiState>

    /** One-time effects. Single collector; buffered while nobody collects; each effect delivered once. */
    val uiEffect: Flow<UiEffect>

    /** The only entry point from the UI. Never suspends, never throws, never blocks. */
    fun onAction(action: UiAction)

    /** Handles one action. Called sequentially, in send order, on viewModelScope. */
    protected abstract suspend fun handleAction(action: UiAction)

    /** Atomically replaces the reducer-owned state. */
    protected fun reduce(reducer: (UiState) -> UiState)

    /** Sends one effect to the effect channel (suspends only while the buffer is full). */
    protected suspend fun emitEffect(effect: UiEffect)

    /** Runs long-running or open-ended work outside the action queue. */
    protected fun launchWork(block: suspend CoroutineScope.() -> Unit): Job
}
```

Nothing else is public or protected. The state holder, the effect channel and the action channel are private.

---

## 3 — Semantics

### 3.1 State

- The reducer-owned state is a `MutableStateFlow(initialState)`. `reduce` applies `MutableStateFlow.update(reducer)`, so concurrent
  reducers (action queue and `launchWork`) never lose an update. `reducer` must be pure and fast.
- `uiState` returns the reducer-owned state by default.
- **Stream-composed ViewModels** (`viewmodel-udf.md` §7) may `override val uiState` with a derived flow, for example
  `combine(super.uiState, dataStream) { local, data -> … }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`.
  `super.uiState` is then the reducer-owned part, and `reduce` keeps working on it. Two consequences for the subclass:
    - `reduce { it … }` receives the reducer-owned state, not the derived one.
    - A derived `WhileSubscribed` state only updates while collected. Tests must subscribe before reading `uiState.value`.
- `UiState` must be immutable (`viewmodel-udf.md` §2). The base class does not copy or validate it.

### 3.2 Action queue

- `Channel<UiAction>(Channel.UNLIMITED)`, consumed by **one** coroutine launched in the base `init` on `viewModelScope`
  (`Dispatchers.Main.immediate`).
- `onAction` calls `trySend`. With an unlimited buffer this never fails while the ViewModel is alive, never suspends and is safe from any
  thread. After the ViewModel is cleared, actions are accepted and never handled (no exception, no log).
- **Order:** actions are handled one at a time, in the order `onAction` was called. A second action does not start until `handleAction`
  for the first one has returned, even if the first one suspends.
- **Eagerness:** when the queue is idle and `onAction` is called on the main thread, `handleAction` starts immediately (before `onAction`
  returns) and runs until its first real suspension. Screens may rely on this the way they relied on `viewModelScope.launch` before.
- **Re-entrancy:** calling `onAction` from inside `handleAction` enqueues the new action after the current one. It is never handled
  recursively.
- **Subclass initialisation order:** the base `init` runs before the subclass's property initialisers. A subclass `init { onAction(…) }`
  handles the action immediately, so it must come **after** every property `handleAction` uses. Put such `init` blocks at the end of the
  class body.
- Keep `handleAction` short. Anything open-ended (flow collection, polling, capture loops, multi-step IO) goes through `launchWork`.

### 3.3 Exception policy and the `CancellationException` guard

The loop processes each action like this:

```kotlin
for (action in actions) {
    try {
        handleAction(action)
    } catch (e: CancellationException) {
        if (!currentCoroutineContext().isActive) throw e // the ViewModel is being cleared: stop the loop
        // Otherwise a collaborator threw CancellationException while the scope is alive: drop this action, keep the queue running.
    }
}
```

**Why.** Before MVI, each ViewModel entry point was its own `viewModelScope.launch { … }`. A `CancellationException` thrown by a
collaborator (for example a repository rethrowing a cancellation from its own scope) ended only that one coroutine; the screen kept
working. With a single queue, the same exception would end the loop, and every later action on that screen would be silently ignored. The
guard keeps the old blast radius: one call is lost, the screen keeps working.

- The guard applies **only** to `CancellationException`, and only while the ViewModel scope is active.
- **Other exceptions are not caught.** They propagate out of the loop to `viewModelScope`, which has no handler, exactly like an exception
  in a plain `viewModelScope.launch` today. Screens that must survive a failure catch it in `handleAction` and model it as state or an
  effect.
- No logging in the guard. A screen that needs to log a failed call logs it at the call site with `Simber`.

### 3.4 Effects

- `Channel<UiEffect>(Channel.BUFFERED)` (64 elements), exposed as `uiEffect = effects.receiveAsFlow()`.
- **Buffered:** effects sent while nobody collects (UI stopped, before the first collector, during a configuration change) are kept and
  delivered, in order, when a collector starts.
- **Delivered once:** each effect goes to exactly one collector and is never replayed. Each ViewModel has exactly one effect collector, its
  screen (`viewmodel-udf.md` §3).
- **Backpressure:** `emitEffect` suspends only when 64 effects are pending. That suspends the caller (the action queue or a `launchWork`
  job) until the UI collects again. Screens must not emit unbounded effect streams; continuous data belongs in state.
- **Never `MutableSharedFlow`** for effects: it drops emissions without collectors.
- **Process death:** effects are lost, like `LiveDataEvent` before. A result or navigation that must survive is modelled as state and
  persisted through `SavedStateHandle` (`viewmodel-udf.md` §3, §4).
- **Clearing:** pending effects are discarded when the ViewModel is cleared.

### 3.5 `launchWork`

- `viewModelScope.launch(block = block)`; returns the `Job`.
- Runs concurrently with the action queue. It may call `reduce` and `emitEffect`, and switch dispatchers with `withContext` (use the
  injected `@DispatcherIO`/`@DispatcherBG` qualifiers).
- Cancelled when the ViewModel is cleared. Normal coroutine exception rules apply; the section 3.3 guard does **not** wrap it (each job is
  already independent).
- To ignore a second start, guard with state (for example `isLoading`) or keep the returned `Job` and check `isActive`.

### 3.6 Threading summary

All base-class bookkeeping runs on `Dispatchers.Main.immediate`. `onAction` and `reduce` are thread-safe. `emitEffect` may be called from
any dispatcher.

---

## 4 — Backing field: preferred form and fallback

- **Preferred** (as written in `viewmodel-udf.md` §3): an explicit backing field,
  `open val uiState: StateFlow<UiState> field = MutableStateFlow(initialState)`, with `reduce` updating it through the smart cast.
  The project already uses explicit backing fields on final properties.
- **Fallback:** if the compiler rejects an explicit backing field on an `open` property, or does not smart-cast it inside `reduce`, use:

  ```kotlin
  private val _uiState = MutableStateFlow(initialState)
  open val uiState: StateFlow<UiState> get() = _uiState
  protected fun reduce(reducer: (UiState) -> UiState) = _uiState.update(reducer)
  ```

- Both forms have the identical public API and semantics; do not add a compiler flag to force the preferred form. Record which form was
  used in the implementing handoff's evidence.

---

## 5 — Tests the base class must pass

File: `BaseMviViewModelTest.kt` (section 1). Plain JVM test, no Robolectric: `TestCoroutineRule` (sets an `UnconfinedTestDispatcher` as
`Main`), `runTest`, Turbine, Truth. The subject is a private test subclass, for example `TestMviViewModel`, whose `handleAction` records
the actions it receives and can suspend on a `CompletableDeferred`, throw on demand, call `reduce`, `emitEffect` and `launchWork`. To clear
it, obtain it through `ViewModelProvider(store, factory)` and call `store.clear()`.

| ID  | Given / when / then                                                                                                                                                                                                 |
|-----|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| B-1 | **Given** a ViewModel built with state `S0`, **then** `uiState.value == S0` before any action.                                                                                                                      |
| B-2 | **When** an action calls `reduce` twice (`+1`, then `*2`), **then** `uiState.value` reflects both, in order.                                                                                                        |
| B-3 | **Given** action A suspends on a deferred, **when** action B is sent, **then** B is not handled until A completes; recorded order is `[A, B]`, and B sees the state A reduced.                                      |
| B-4 | **When** an action emits effect E before anything collects `uiEffect`, **then** a collector started afterwards receives E.                                                                                          |
| B-5 | **Given** a first collector received E and was cancelled, **when** a second collector starts, **then** it receives nothing (no replay).                                                                             |
| B-6 | **Given** a collector is cancelled, **when** effects E1, E2 are emitted, **then** a new collector receives E1 then E2, each once.                                                                                   |
| B-7 | **Given** action A starts `launchWork` that suspends on a deferred, **when** action B is sent, **then** B is handled immediately while the work is still running.                                                   |
| B-8 | **Given** action A throws `CancellationException` while the ViewModel is active, **when** action B is sent, **then** B is handled (the queue survives) and A's partial state is whatever A reduced before throwing. |
| B-9 | **Given** the `ViewModelStore` is cleared, **when** an action is sent, **then** it is not handled; a running `launchWork` job is cancelled.                                                                         |

Non-cancellation exceptions are deliberately not tested: they reach the uncaught-exception path exactly like a plain
`viewModelScope.launch`, which the test framework reports as a failure of an unrelated test.

---

## 6 — Shared test helper for Fragment tests

Fragment tests replace the screen ViewModel with a MockK mock (`@BindValue`). A relaxed mock of `StateFlow` never emits, and
`StateFlow.collect` returns `Nothing`, so every Fragment test must stub both streams explicitly. Each module adds this helper to its own
test tools package (the pilot: `feature/dashboard/src/test/java/com/simprints/feature/dashboard/tools/MviTestExt.kt`). It is kept
module-local because `:infra:test-tools` does not depend on `:infra:ui-base`; moving it there is a separate change.

```kotlin
internal class MviStubs<S : Any, E : Any>(initial: S) {
    val state = MutableStateFlow(initial)
    val effects = Channel<E>(Channel.BUFFERED)
}

/** Call on a MockK mock of a BaseMviViewModel subclass. Stubs uiState and uiEffect; onAction stays a recorded no-op. */
internal fun <S : Any, A : Any, E : Any> BaseMviViewModel<S, A, E>.stubMvi(initial: S): MviStubs<S, E> =
    MviStubs<S, E>(initial).also { stubs ->
        every { this@stubMvi.uiState } returns stubs.state
        every { this@stubMvi.uiEffect } returns stubs.effects.receiveAsFlow()
    }
```

Usage rules:

- Push new state with `stubs.state.value = …`; push effects with `stubs.effects.trySend(…)`.
- Assert input with `verify { viewModel.onAction(SomeAction) }`.
- Never rely on an unstubbed relaxed mock for `uiState` or `uiEffect`.
- A test that asserts "not displayed" on a view the XML already hides must first render a state that shows it (visible → hidden), or the
  assertion proves nothing.

---

## 7 — Checklist for a module migration

- [ ] Every screen ViewModel extends `BaseMviViewModel`; no module defines its own base or effect transport.
- [ ] `init { onAction(…) }` blocks come after the properties `handleAction` uses (section 3.2).
- [ ] Open-ended work uses `launchWork`; `handleAction` stays short.
- [ ] Failures that must not crash are caught in `handleAction` and turned into state or effects (section 3.3).
- [ ] Stream-composed screens override `uiState` with `stateIn(…, WhileSubscribed(5_000), …)`; their tests subscribe before reading.
- [ ] Fragment tests stub `uiState` and `uiEffect` with the section 6 helper.
