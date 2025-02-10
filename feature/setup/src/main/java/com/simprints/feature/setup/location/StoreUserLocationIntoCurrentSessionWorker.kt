package com.simprints.feature.setup.location

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.simprints.core.DispatcherMain
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.events.event.domain.models.scope.Location
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.logging.Simber
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext

/**
 * Worker that collects user's last known location and save it into current session
 */
@HiltWorker
internal class StoreUserLocationIntoCurrentSessionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val eventRepository: SessionEventRepository,
    private val locationManager: LocationManager,
    @DispatcherMain private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(context, params) {
    override val tag: String = "StoreUserLocationWorker"

    override suspend fun doWork(): Result = withContext(dispatcher) {
        crashlyticsLog("Started")
        showProgressNotification()
        try {
            createLocationFlow()
                .filterNotNull()
                .collect { location ->
                    runCatching { saveUserLocation(location) }
                }
        } catch (t: Throwable) {
            fail(t)
        }
        success()
    }

    private fun createLocationFlow(): Flow<android.location.Location?> {
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, DEFAULT_INTERVAL).build()
        return locationManager.requestLocation(locationRequest).take(1)
    }

    private suspend fun saveUserLocation(lastLocation: android.location.Location) {
        if (!isStopped) {
            // Only store location if SID didn't yet sent the response to the calling app
            val sessionScope = eventRepository.getCurrentSessionScope()
            val updatesSessionScope = sessionScope.copy(
                payload = sessionScope.payload.copy(
                    location = Location(lastLocation.latitude, lastLocation.longitude),
                ),
            )
            eventRepository.saveSessionScope(updatesSessionScope)
            Simber.d("Saving user's location into the current session", tag = tag)
        }
    }

    companion object {
        // Based on the default value of minUpdateIntervalMillis in LocationRequest
        private const val DEFAULT_INTERVAL = 10 * 60 * 1000L
    }
}
