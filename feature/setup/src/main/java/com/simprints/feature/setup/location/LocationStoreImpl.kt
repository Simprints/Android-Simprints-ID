package com.simprints.feature.setup.location

import com.simprints.core.AppScope
import com.simprints.feature.setup.LocationStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LocationStoreImpl @Inject constructor(
    @AppScope appScope: CoroutineScope,
    private val collectLocation: CollectLocationUseCase,
) : LocationStore {
    // A child scope of the app scope so that collection coroutines can be cancelled
    private val scope = CoroutineScope(appScope.coroutineContext + SupervisorJob(appScope.coroutineContext[Job]))

    override fun collectLocationInBackground() {
        scope.coroutineContext.cancelChildren()
        scope.launch { collectLocation() }
    }

    override fun cancelLocationCollection() {
        scope.coroutineContext.cancelChildren()
    }
}
