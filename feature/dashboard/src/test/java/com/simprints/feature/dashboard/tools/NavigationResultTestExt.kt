package com.simprints.feature.dashboard.tools

import androidx.annotation.IdRes
import androidx.navigation.NavController
import java.io.Serializable

/**
 * Seeds a navigation result where `NavController.handleResult` (`:infra:ui-base`) reads it: the key `"result-<targetDestinationId>"` in the
 * `SavedStateHandle` of the back-stack entry of [currentDestinationId]. Seed before the receiving view is created; `handleResult` reads it
 * when the listener is registered.
 */
internal fun NavController.seedNavigationResult(
    @IdRes currentDestinationId: Int,
    @IdRes targetDestinationId: Int,
    result: Serializable,
) {
    getBackStackEntry(currentDestinationId).savedStateHandle["result-$targetDestinationId"] = result
}
