package com.simprints.feature.setup.location

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.simprints.core.DispatcherMain
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.events.event.domain.models.scope.Location
import com.simprints.infra.logging.Simber
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext

/**
 * Worker that collects user's last known location and save it into current session
 */
@HiltWorker
internal class StoreUserLocationIntoCurrentSessionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val updateSessionScopeLocationUseCase: UpdateSessionScopeLocationUseCase,
    private val locationManager: LocationManager,
    @param:DispatcherMain private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(context, params) {
    override val tag: String = "StoreUserLocationWorker"

    override suspend fun doWork(): Result = withContext(dispatcher) {
        showProgressNotification()
        crashlyticsLog("Started")
        try {
            locationManager
                .requestLocation()
                .filterNotNull()
                .collect { location -> runCatching { saveUserLocation(location) } }
        } catch (t: Throwable) {
            fail(t)
        }
        success()
    }

    private suspend fun saveUserLocation(location: Location) {
        if (!isStopped) {
            // Only store location if SID didn't yet sent the response to the calling app
            updateSessionScopeLocationUseCase(location)
            Simber.d("Saving user's location into the current session", tag = tag)
        }
    }
}
