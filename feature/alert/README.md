# Alert screen

This module provides configurable alert screen that can
be integrate in other feature modules when necessary.

Alert screen consists of following UI elements:

* One of pre-defined background colors
* Title on top of the screen
* Large central icon (scaled according to the smallest screen side)
* Longer message under the main icon with optional icon on the left side
* One or two configurable action buttons (defaults to single "Close" button)

In addition to showing the alert UI this screen automatically logs error event (if provided).

When defining action buttons custom action keys and payload can be provided.
It will be returned to the caller
via [Fragment result API](https://developer.android.com/guide/fragments/communicate#fragment-result)
to be handled explicitly on callers side.
Alert screen does not handle any pre-defined action besides closing itself.

## Usage

1. Add module to gradle of the caller module

```kotlin
// In module/build.gradle.kts

implementation(project(":feature:alert"))
```

2. Include alert sub-graph and define actions to navigate to the alert screen

```xml
<!-- In graph.xml -->

<navigation>
    <include app:graph="@navigation/graph_alert" />

    <action android:id="@+id/action_global_errorFragment" app:destination="@id/graph_alert"
        app:popUpTo="@id/root_graph" app:popUpToInclusive="true" />
</navigation>
```

3. Call navigate on the modules nav controller

```kotlin
// Handle action button clicks

// In orchestrator activity
binding.orchestratorHostFragment
    .handleResult<AlertResult>(this, AlertContract.ALERT_DESTINATION) { }

// In fragment within navigation graph
findNavController().handleResult<AlertResult>(
    this, 
    R.id.currentScreen,
    AlertContract.ALERT_DESTINATION,
) { }

// Navigate to screen
findNavController(R.id.host_fragment).navigateSafely(
    this,
    R.id.action_global_errorFragment,
    alertConfiguration { /* configuration */ }.toArgs(),
)
```

4. If module is not using fragment navigation `ShowAlertWrapper` contract can be used instead.
