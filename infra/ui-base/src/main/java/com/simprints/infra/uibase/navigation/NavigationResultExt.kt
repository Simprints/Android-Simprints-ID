package com.simprints.infra.uibase.navigation

import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports
import java.io.Serializable

/**
 * Add lifecycle aware fragment result listener for a provided destination ID for the navigation host controller.
 * This listener acts like bridge between hosting activity and navigation graph.
 *
 * Use this method to handle result from the root fragment of the navigation graph within an activity.
 *
 * Handler will be invoked when the result is set in the calling fragment.
 */
@ExcludedFromGeneratedTestCoverageReports("There is no reasonable way to test this")
fun <T : Serializable> FragmentContainerView.handleResult(
    lifecycleOwner: LifecycleOwner,
    @IdRes targetDestinationId: Int,
    handler: (T) -> Unit
) {
    val expectedResultKey = resultName(targetDestinationId)
    getFragment<Fragment>().childFragmentManager
        .setFragmentResultListener(expectedResultKey, lifecycleOwner) { key, resultBundle ->
            (resultBundle.getSerializable(key) as? T)?.let(handler)
        }
}

/**
 * Add fragment result listener directly to the calling fragment.
 * This function should be used only in fragment tests to verify correct results are being returned.
 */
@ExcludedFromGeneratedTestCoverageReports("There is no reasonable way to test this")
fun <T : Serializable> Fragment.handleResultDirectly(@IdRes targetDestinationId: Int, handler: (T) -> Unit) {
    val expectedResultKey = resultName(targetDestinationId)
    setFragmentResultListener(expectedResultKey) { key, resultBundle ->
        (resultBundle.getSerializable(key) as? T)?.let(handler)
    }
}

/**
 * Add a listener for fragment result only within navigation graph (including sub-graphs).
 *
 * When navigating to a nested graph for result do not use "popUpTo=graph" as it prevents target
 * destination from being added to the backstack and makes result delivery impossible.
 *
 * Handler will be invoked when parent fragment is restored.
 */
@Suppress("UsePropertyAccessSyntax") // compiler is confused by `lifecycle` getter
@ExcludedFromGeneratedTestCoverageReports("There is no reasonable way to test this")
fun <T : Serializable> NavController.handleResult(
    lifecycleOwner: LifecycleOwner,
    @IdRes currentDestinationId: Int,
    @IdRes targetDestinationId: Int,
    handler: (T) -> Unit
) {
    // Do not handle anything if current destination is not available in the stack or there is no backstack
    currentDestination ?: return
    findDestination(currentDestinationId) ?: return

    // `getCurrentBackStackEntry` doesn't work in case of recovery from the process death when dialog is opened.
    val currentEntry = getBackStackEntry(currentDestinationId)

    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            handleResultFromChild(targetDestinationId, currentEntry, handler)
        }
    }
    currentEntry.lifecycle.addObserver(observer)
    lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            currentEntry.lifecycle.removeObserver(observer)
        }
    })
}

private fun <T : Serializable> handleResultFromChild(
    @IdRes childDestinationId: Int,
    currentEntry: NavBackStackEntry,
    handler: (T) -> Unit
) {
    val expectedResultKey = resultName(childDestinationId)

    with(currentEntry.savedStateHandle) {
        if (contains(expectedResultKey)) {
            get<T>(expectedResultKey)?.let(handler)
            remove<T>(expectedResultKey)
        }
    }
}

/**
 * Sets the provided Serializable as a fragment result to be used both
 * within and outside of the navigation graph.
 */
@ExcludedFromGeneratedTestCoverageReports("There is no reasonable way to test this")
fun <T : Serializable> NavController.setResult(fragment: Fragment, result: T) {
    val currentDestinationId = currentDestination?.id
    if (currentDestinationId != null) {
        val resultName = resultName(currentDestinationId)

        // Set results into correct navigation stack entry
        previousBackStackEntry?.savedStateHandle?.set(resultName, result)
        // Send result to fragment result listeners
        fragment.setFragmentResult(resultName, bundleOf(resultName to result))
    }
}

/**
 * Same as `setResult()` but also pops current fragment from backstack
 *
 * @return true if the stack was popped at least once
 */
@ExcludedFromGeneratedTestCoverageReports("There is no reasonable way to test this")
fun <T : Serializable> NavController.finishWithResult(fragment: Fragment, result: T): Boolean {
    val currentDestinationId = currentDestination?.id
    val saveHandle = previousBackStackEntry?.savedStateHandle

    // Result listener is set via FragmentContainerView it will be executed as soon as setFragmentResult is called.
    // Therefore popping the stack first to make sure that navigation always happens before the result callback.
    val popped = popBackStack()

    if (currentDestinationId != null) {
        val resultName = resultName(currentDestinationId)

        // Set results into correct navigation stack entry
        saveHandle?.set(resultName, result)
        // Send result to fragment result listeners
        fragment.setFragmentResult(resultName, bundleOf(resultName to result))
    }
    return popped
}

private fun resultName(resultSourceId: Int) = "result-$resultSourceId"
