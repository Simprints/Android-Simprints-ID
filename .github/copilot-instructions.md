1. Code Style

* Code follows general Kotlin code style recommendations.
* Use Android Jetpack ViewModel for managing UI-related data and use viewModelScope for coroutines.
* Use LiveData for observing and updating UI state. Use Custom event wrappers `LiveDataEventWithContent` to handle one-time events.
* Use delegates like `FragmentViewBindingDelegate` to simplify view binding in fragments.

2. Architecture Layers:

* Feature Modules: Manages UI components and ViewModels as well as UI-dependant features. Implemented in `:feature` subfolder.
* Infra Modules: Contains use cases and core business logic. Implemented in `:infra` subfolder.
* Modality Modules: Contain business subsets of feature and infra modules related to a specific biometric modality. Implemented in `:face`
  and `:fingerprint` subfolder.
* Each layer is isolated. Feature modules depend on other features and infra modules, infra modules only depend on other infra modules.

3. Dependency Injection:

* Use Dagger Hilt for dependency injection with the `@Inject` constructor as the preferred method of injection.
* Use Singleton scope (`@Singleton`) for critical dependencies.
* ViewModels are injected using Hilt (`@HiltViewModel`).

4. Unit testing rules:

* Use JUnit 4 for unit testing, MockK for mocking, Google Truth for assertions, AndroidX Test for Android-specific testing.
* Test classes use the `*Test` suffix. Test methods use descriptive names with backticks.
* Setup logic is in `@Before` methods. Tests use `@RunWith(AndroidJUnit4::class)` where appropriate. Coroutine tests use `runTest { ... }`.
* Use Given-When-Then structure. Keep tests concise and focused. Use relaxed mocks to reduce boilerplate.
* Use helper methods for mock objects or test data.
* Use lifecycle-aware and coroutine testing where appropriate.
* Isolate Android framework dependencies unless required.
