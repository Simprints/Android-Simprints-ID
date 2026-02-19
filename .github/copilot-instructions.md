## 1. Code Style

* Code follows general Kotlin code style recommendations.
* Use Android Jetpack ViewModel for managing UI-related data and use `viewModelScope` for coroutines.
* Use `LiveData` for observing and updating UI state. Use custom event wrappers `LiveDataEventWithContent` and `LiveDataEvent` (in `infra/core`) to handle one-time events.
* Use `FragmentViewBindingDelegate` (`by viewBinding(...)`) to simplify view binding in fragments.
* All UI is View-based (XML layouts + ViewBinding).
* Use `internal` visibility for module-internal classes (fragments, view models, helpers) to enforce module boundaries. Only `Contract` objects and public API types should be `public`.
* Use `Simber` (in `infra/logging`) for logging instead of `Log` or `println`.

## 2. Architecture Layers

* **Feature Modules** (`:feature:*`): UI components, Fragments, ViewModels, and UI-specific use cases. Applied via `simprints.feature` convention plugin.
* **Infra Modules** (`:infra:*`): Core business logic, repositories, use cases, data layer. Applied via `simprints.infra` convention plugin.
* **Modality Modules** (`:face:*`, `:fingerprint:*`): Biometric modality-specific subsets of feature and infra modules.
* **App Module** (`:id`): Application entry point with Firebase, Crashlytics, and Play Store deployment config.
* Each layer is isolated. Feature modules depend on other features and infra modules; infra modules only depend on other infra modules.

## 3. Module Communication & Navigation

* Each feature module exposes a public `Contract` object (e.g., `ConsentContract`, `LoginContract`) defining:
  * `DESTINATION` — the navigation destination ID.
  * `getParams(...)` — a factory method to create type-safe navigation arguments.
* Each feature module has its own navigation graph in `src/main/res/navigation/graph_*.xml`.
* Use Jetpack Navigation with SafeArgs (auto-applied by `simprints.feature` plugin).
* Fragment-level result handling supports cross-navigation-graph communication via `SavedStateHandle` with `FragmentResultListener` fallback for process death resilience.
* The orchestrator (`feature/orchestrator`) acts as a central coordinator, dynamically navigating to feature graphs and handling results based on a step-based state machine.
* Use `navigateSafely()` for navigation, `finishWithResult()` to return results, and `handleResult<T>()` to receive results. All defined in `infra/ui-base/navigation/NavigationResultExt.kt`.

## 4. Build System

* Convention plugins are defined in `build-logic/convention/` and applied via plugin IDs like `simprints.feature`, `simprints.infra`, `simprints.library.room`, `simprints.library.hilt`, etc.
* Version catalog is in `gradle/libs.versions.toml`.
* Build types: `debug`, `staging` (minified), `release` (minified, `@StopShip` lint fatal).
* Room modules use SQLCipher encryption controlled by `DB_ENCRYPTION` build config.

## 5. Dependency Injection

* Use Dagger Hilt for dependency injection with `@Inject` constructor as the preferred method.
* All Hilt modules use `@InstallIn(SingletonComponent::class)`. Use `@Singleton` for critical dependencies.
* ViewModels are annotated with `@HiltViewModel` and injected via `@Inject constructor`.
* Use `@Binds` (abstract module) for interface-to-implementation bindings. Use `@Provides` (object module) for factory methods.
* Custom dispatcher qualifiers: `@DispatcherIO`, `@DispatcherMain`, `@DispatcherBG` for coroutine scopes.

## 6. Serialization

* Use `kotlinx.serialization` with the global `SimJson` instance (in `infra/serialization`).
* Domain models use `@Serializable` annotations with compile-time polymorphic serialization.
* Use `@SerialName` for custom JSON field names. When referencing companion object constants in `@SerialName`, use direct reference (e.g., `BiometricReferenceType.FACE_REFERENCE_KEY`) without `.Companion`.

## 7. Data Persistence

* **Room**: Used for events, logs, image metadata, and enrolment records. Modules apply `simprints.library.room` plugin.
* **Realm**: Deprecated database for enrolment record storage (`infra/enrolment-records/realm-store`). Will be removed in a future release.
* **DataStore/Protobuf**: Used for config and auth stores via `simprints.library.protobuf` plugin.

## 8. Network

* Retrofit with `kotlinx.serialization` converter, OkHttp client with retry logic (5 attempts default, exponential backoff).
* API calls return `ApiResult<T>` (sealed class with `Success`/`Failure`) from `infra/backend-api`.
* All network calls are `suspend` functions. Use `withContext(Dispatchers.IO)` for network operations.
* `SimCoroutineWorker` (in `infra/core`) is the base class for WorkManager workers, providing structured `retry()`, `fail()`, `success()` result helpers and foreground notification support.

## 9. Error Handling

* Use `ApiResult<T>` sealed class for network error handling with `getOrThrow()` and `getOrMapFailure()`.
* Specific exceptions: `BackendMaintenanceException`, `NetworkConnectionException`, `RetryableCloudException`.
* Network-related exceptions (timeout, SSL, unknown host) are downgraded from error to info severity in `Simber` to reduce Crashlytics noise.

## 10. Coroutines

* Use `viewModelScope` for ViewModel coroutines. Use `runTest { ... }` for coroutine tests.
* Functions returning `Flow` should **not** be marked `suspend` — expose them as plain functions returning `Flow`.
* Use `StateFlow` and `SharedFlow` for reactive state in ViewModels.

## 11. Unit Testing

* Use JUnit 4, MockK for mocking, Google Truth for assertions, AndroidX Test and Robolectric for Android-specific testing.
* Test classes use the `*Test` suffix. Test methods use descriptive names with backticks.
* Setup logic is in `@Before` methods. Tests use `@RunWith(AndroidJUnit4::class)` where appropriate.
* Use Given-When-Then structure. Keep tests concise and focused. Use `@RelaxedMockK` / relaxed mocks to reduce boilerplate.
* Use `TestCoroutineRule` (in `infra/test-tools`) — sets `UnconfinedTestDispatcher` as `Dispatchers.Main`.
* Use `InstantTaskExecutorRule` for synchronous LiveData execution in tests.
* LiveData assertions: `assertEventReceived()`, `assertEventReceivedWithContent()`, `getOrAwaitValue()` from `infra/test-tools`.
* Use `@TestInstallIn` with fake Hilt modules for DI replacement in tests.
* Fragments, Activities, and Hilt Modules are excluded from SonarCloud coverage via project-level settings. Use `@ExcludedFromGeneratedTestCoverageReports("UI class")` to exclude other UI-adjacent classes (adapters, view helpers, etc.).

## 12. Event System

* The project uses a custom event system (`infra/events`) to track user actions, biometric captures, matches, and workflow steps as structured, auditable events.
See [event-system.md](.github/event-system.md) for details, including event hierarchy, scopes, persistence, sync, and tokenization.
