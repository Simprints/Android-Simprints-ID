# Compose UI Testing with UDF State

Reference document for testing patterns introduced during Compose migration. This migration introduces a **new UI testing approach** that
differs fundamentally from the legacy `LiveData`-based pattern used in View-based XML fragments.

---

## What Changed

| Aspect                        | Legacy (View + LiveData)                                                | New (Compose + UDF)                                            |
|-------------------------------|-------------------------------------------------------------------------|----------------------------------------------------------------|
| **State observation**         | `LiveData.getOrAwaitValue()` (blocking)                                 | `collectAsStateWithLifecycle()` (reactive collection)          |
| **Effects (one-time events)** | `LiveDataEvent` wrapper + `getOrAwaitValue()`                           | `Flow<UiEffect>` + `LaunchedEffect` collection                 |
| **Test helpers**              | `InstantTaskExecutorRule` + `getOrAwaitValue()` from `infra/test-tools` | None—use standard Compose `@Composable` testing + Turbine      |
| **Rendering**                 | Fragment layout inflation + assertions on View properties               | Compose `ComposeTestRule` + `printToLog()` + semantic matchers |

---

## Testing UiState in Compose

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

## Testing UiEffect (One-Time Events)

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

## Testing with Turbine (Advanced)

For **unit tests of ViewModel logic** (not Compose UI rendering), use **[Turbine](https://github.com/cashapp/turbine)** to assert Flow
emissions:

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

---

## Key Differences from Legacy Testing

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

## Migration Checklist for ViewModel Tests

When migrating a ViewModel's tests from `LiveData` to UDF:

- [ ] Replace `InstantTaskExecutorRule` with `ComposeTestRule` or `runTest { }` block
- [ ] Replace `liveData.getOrAwaitValue()` with state Flow collection via `collectAsStateWithLifecycle()` (Compose tests) or Turbine (unit
  tests)
- [ ] Replace `LiveDataEvent` assertions with `Flow<UiEffect>` collection in `LaunchedEffect`
- [ ] Remove old `LiveData` test helpers; use Turbine for Flow assertions
- [ ] Add `@get:Rule val composeTestRule = createComposeRule()` to Compose UI tests
- [ ] Verify navigation effects are tested via orchestrator result handling, not direct effect assertion
- [ ] Run full test suite to ensure no timing issues (use `waitUntil { }` for async state changes)

---

**Related references:**

- See [compose-viewmodel-udf.md](compose-viewmodel-udf.md#2f--testing-the-mvi-contract) for the
  canonical ViewModel UDF testing guide and effect-transport decision.
