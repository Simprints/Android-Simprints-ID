package com.simprints.id.services.location

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.session.Location
import com.simprints.id.services.sync.events.common.SimCoroutineWorker
import com.simprints.id.tools.LocationManager
import com.simprints.infra.logging.Simber
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext

const val STORE_USER_LOCATION_WORKER_TAG = "StoreUserLocationWorkerTag"

/**
 * Worker that collects user's last known location and save it into current session
 */
@HiltWorker
class StoreUserLocationIntoCurrentSessionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val eventRepository: EventRepository,
    private val locationManager: LocationManager,
    private val dispatcherProvider: DispatcherProvider,
) :
    SimCoroutineWorker(context, params) {


    override val tag: String = StoreUserLocationIntoCurrentSessionWorker::class.java.simpleName

    override suspend fun doWork(): Result =
        withContext(dispatcherProvider.main()) {
            try {
                val locationsFlow = createLocationFlow()
                locationsFlow.filterNotNull().collect { location ->
                    runCatching {
                        saveUserLocation(location)
                    }
                }
            } catch (t: Throwable) {
                Simber.e(t)
                fail(t)
            }
            success()
        }

    private fun createLocationFlow(): Flow<android.location.Location?> {
        val locationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
        return locationManager.requestLocation(locationRequest).take(1)
    }

    private suspend fun saveUserLocation(lastLocation: android.location.Location) {
        if (!isStopped) { // Only store location if SID didn't yet sent the response to the calling app
            val currentSession = eventRepository.getCurrentCaptureSessionEvent()
            currentSession.payload.location =
                Location(lastLocation.latitude, lastLocation.longitude)
            eventRepository.addOrUpdateEvent(currentSession)
            Simber.d("Saving user's location into the current session")
        }
    }
}
