# Exit form screen

This module provides configurable exit form screen that can
be integrate in other feature modules when necessary.

Exit form consists of following UI elements:

* Title on top of the screen
* List of pre-defined refusal option that can be hidden if not relevant
* Back button with configurable text
* Submit button

Screen logs submitted refusal option with relevant event as well as returning it
via [Fragment result API](https://developer.android.com/guide/fragments/communicate#fragment-result)
so that caller could resume execution flow as usual.

## Usage

1. Add module to gradle of the caller module

```kotlin
// In module/build.gradle.kts

implementation(project(":feature:exit-form"))
```

2. Include exit form sub-graph and define actions to navigate to the exit form screen

```xml
<!-- In graph.xml -->

<navigation>
    <include app:graph="@navigation/graph_exit_form" />

    <action android:id="@+id/action_global_refusalFragment" app:destination="@id/graph_exit_form"
        app:popUpTo="@id/root_graph" />
</navigation>
```

3. Call navigate on the modules nav controller.

```kotlin
// Handle result

// In orchestrator activity
binding.orchestratorHostFragment
    .handleResult<ExitFormResult>(this, ExitFormContract.DESTINATION) { }

// In fragment within navigation graph
findNavController().handleResult<ExitFormResult>(
    this,
    R.id.currentScreen,
    ExitFormContract.DESTINATION,
) { }

// Navigate to screen
findNavController(R.id.host_fragment).navigateSafely(
    this,
    R.id.action_global_refusalFragment,
    exitFormConfiguration { /* configuration */ }.toArgs(),
)
```

4. If module is not using fragment navigation `ExitFormWrapperActivity` contract can be used
   instead.
