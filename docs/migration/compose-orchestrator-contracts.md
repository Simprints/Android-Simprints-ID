# Orchestrator Contract Guardrails

> **Applies to all modules except `:feature:dashboard`.**
> Dashboard is the standalone UI hub, not an orchestrator step.
> Every other feature module is called as a step in a dynamically built step list
> and must preserve full contract compatibility with `OrchestratorFragment`.

---

## How the orchestrator dispatches steps

```
OrchestratorViewModel.handleAction(action)
  └─ BuildStepsUseCase.build(...)         ← builds List<Step> from project config
       └─ Step(id, navigationActionId, destinationId, params, status, result)
            │
OrchestratorFragment observes currentStep
  └─ navigateSafely(actionId = step.navigationActionId,
                    args    = step.params.toBundle())   ← params must be Parcelable/Bundle
            │
Feature Fragment/screen executes, then:
  └─ finishWithResult(this, FooResult(...))             ← result via SavedStateHandle
            │
OrchestratorFragment.handleResult<FooResult>(FooContract.DESTINATION) { result ->
  └─ orchestratorVm.handleResult(result)               ← result typed as StepResult
            │
OrchestratorCache persists steps as JSON (encrypted SharedPreferences)
  └─ uses orchestratorSerializersModule for polymorphic serialisation
```

---

## Guardrail 1 — Contract object: shape must not change

```kotlin
// REQUIRED shape — do not rename, restructure, or split
object FooContract {
    val DESTINATION = R.id.fooFragment   // ← @IdRes; MUST stay the same Int value
    fun getParams(...): FooParams = FooParams(...)  // ← factory; signature may evolve
    // Optional result key constants (AlertContract.ALERT_BUTTON_PRESSED_BACK pattern)
}
```

- `DESTINATION` references the **navigation graph fragment/composable destination ID**.
  If the interop Fragment is renamed, the destination ID in `graph_foo.xml` must not change.
- `getParams()` is called by `BuildStepsUseCase` — its parameter signature may gain new
  optional arguments, but must remain backwards-compatible.

---

## Guardrail 2 — StepParams: serialisation annotations must be preserved exactly

Each module's params class is cached as JSON in `OrchestratorCache` and must survive process
death and app restarts. **Any change to `@SerialName` is a cache-breaking migration.**

```kotlin
// Mandatory annotations — all three required, none may be removed or altered
@Keep                          // prevents R8 from renaming the class
@Serializable                  // kotlinx.serialization
@SerialName("ConsentParams")   // ← stable JSON discriminator; NEVER change this string
data class FooParams(
    val someField: String,
) : StepParams                 // ← MUST implement StepParams (from :infra:core)
```

- `@SerialName` must equal the **original class name string** registered in
  `orchestratorSerializersModule`. Changing it silently breaks deserialisation of cached steps.
- If a field is added, use `val newField: Type = defaultValue` so old cached JSON still parses.
- If a field is removed, keep it with `@Deprecated` + a default until the cache TTL has elapsed.

---

## Guardrail 3 — StepResult: same rules as StepParams

```kotlin
@Keep
@Serializable
@SerialName("FooResult")       // ← never change
data class FooResult(
    val someOutcome: Boolean,
) : StepResult                 // ← MUST implement StepResult (from :infra:core)
```

- Results from **all steps** are passed to `AppResponseBuilderUseCase` at the end of the flow.
  Any missing or type-changed result will break response building silently.
- `ExitFormResult` and error-producing results short-circuit the flow via
  `MapRefusalOrErrorResultUseCase` — this logic lives in the orchestrator and must not be
  replicated in the feature module.

---

## Guardrail 4 — Serialiser registration in `orchestratorSerializersModule`

When migrating a module, verify that its `StepParams` and `StepResult` are registered in
`feature/orchestrator/src/main/java/.../steps/Step.kt`:

```kotlin
val orchestratorSerializersModule = SerializersModule {
    polymorphic(StepResult::class) {
        subclass(FooResult::class)      // ← must be present
    }
    polymorphic(StepParams::class) {
        subclass(FooParams::class)      // ← must be present
    }
}
```

If a new result or params type is introduced during migration, register it here. **Do not remove
existing registrations** — old cached data may still reference them.

---

## Guardrail 5 — Navigation destination ID stability

The orchestrator navigates using `step.navigationActionId` (a nav graph action ID) and
`step.destinationId` (the destination fragment/composable ID). Both are `@IdRes Int` values
defined in XML nav graphs.

During Compose migration using the interop Fragment shell approach:

- **Keep `graph_foo.xml`** and the existing `<fragment android:id="@+id/fooFragment">` entry.
- The interop Fragment class name may change, but its nav graph declaration must keep the
  same `android:id`.
- The nav graph action IDs (`R.id.action_..._to_fooFragment`) must remain stable.
- **Do not delete `graph_foo.xml`** until Navigation 3 cutover (Phase 6), which requires
  coordinated updates to `BuildStepsUseCase` and `OrchestratorFragment` simultaneously.

---

## Guardrail 6 — Result return mechanism: `finishWithResult()` only

Feature modules must return results **exclusively** via:

```kotlin
// In the interop Fragment shell
findNavController().finishWithResult(this, FooResult(...))
```

- Do **not** use `Activity.setResult()` — it bypasses the `SavedStateHandle` mechanism.
- Do **not** share result state via a shared ViewModel — results must flow through the
  `handleResult<FooResult>(FooContract.DESTINATION)` binding in `OrchestratorFragment`.
- The Composable screen should call a lambda (`onComplete: (FooResult) -> Unit`) injected by
  the interop Fragment; the Fragment then calls `finishWithResult()`.

---

## Guardrail 7 — `OrchestratorFragment` `handleResult` registration

`OrchestratorFragment` has an explicit `handleResult` binding for every orchestrated step.
When adding a new step or renaming a module, verify the binding exists:

```kotlin
// In OrchestratorFragment.onViewCreated()
handleResult(FooContract.DESTINATION, orchestratorVm::handleResult)
```

This line must be present for the result to reach `OrchestratorViewModel.handleResult()`.
Omitting it means the step result is silently discarded and the orchestrator stalls.

---

## Per-module contract checklist

Run this checklist before marking any module migration as complete (Stages 2–6):

- [ ] `FooContract.DESTINATION` value unchanged
- [ ] `FooContract.getParams(...)` callable with same arguments from `BuildStepsUseCase`
- [ ] `FooParams` has `@Keep`, `@Serializable`, `@SerialName("FooParams")` — string unchanged
- [ ] `FooParams` extends `StepParams` from `:infra:core`
- [ ] `FooResult` has `@Keep`, `@Serializable`, `@SerialName("FooResult")` — string unchanged
- [ ] `FooResult` extends `StepResult` from `:infra:core`
- [ ] Both registered in `orchestratorSerializersModule` in `Step.kt`
- [ ] Nav graph XML keeps `android:id="@+id/fooFragment"` on the destination entry
- [ ] Interop Fragment calls `finishWithResult(this, FooResult(...))` — not `Activity.setResult()`
- [ ] `OrchestratorFragment` has `handleResult(FooContract.DESTINATION, orchestratorVm::handleResult)`
- [ ] `./gradlew :feature:orchestrator:test` passes after module migration
