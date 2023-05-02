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

implementation(project(":featurexitform"))
```

2. Include exit form sub-graph and define actions to navigate to the exit form screen

```xml
<!-- In graph.xml -->

<navigation>
    <include app:graph="@navigation/graph_exit_form" />

    <action android:id="@+id/action_global_refusalFragment" app:destination="@id/graph_exit_form"
        app:popUpTo="@id/root_graph" app:popUpToInclusive="true" />
</navigation>
```

3. Call navigate on the modules nav controller.

```kotlin
// In orchestrator fragment/activity

// Handle action button clicks
childFragmentManager.setFragmentResultListener(ExitFormContract.EXIT_FORM_REQUEST, this@lifecycleOwner) { _, d ->
    val formSubmitted = ExitFormContract.isFormSubmitted(d)
    val option = ExitFormContract.getFormOption(d)
    val reason = ExitFormContract.getFormReason(d).orEmpty()

    // Note that caller is responsible for resuming the flow even if form was not submitted
}

// Navigate to screen
findNavController(R.id.host_fragment).navigate(
    R.id.action_global_refusalFragment,
    exitFormConfiguration { /* configuration */ }.toArgs(),
)
```

4. If module is not using fragment navigation `ExitFormWrapperActivity` contract can be used
   instead.
