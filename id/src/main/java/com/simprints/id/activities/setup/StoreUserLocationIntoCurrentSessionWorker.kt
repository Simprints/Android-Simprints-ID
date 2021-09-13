package com.simprints.id.activities.setup

import android.content.Context
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationRequest
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.session.Location
import com.simprints.id.services.sync.events.common.SimCoroutineWorker
import com.simprints.id.tools.LocationManager
import com.simprints.logging.Simber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StoreUserLocationIntoCurrentSessionWorker(context: Context, params: WorkerParameters) :
    SimCoroutineWorker(context, params) {

    override val tag: String = StoreUserLocationIntoCurrentSessionWorker::class.java.simpleName
    @Inject
    lateinit var eventRepository: EventRepository
    @Inject
    lateinit var locationManager: LocationManager

    override suspend fun doWork(): Result = withContext(Dispatchers.Main) {
        getComponent<StoreUserLocationIntoCurrentSessionWorker> { it.inject(this@StoreUserLocationIntoCurrentSessionWorker) }
        try {
            val locationRequest = LocationRequest().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            val locationsFlow = locationManager.requestLocation(locationRequest).take(1)
            locationsFlow.collect { locations ->
                saveUserLocation(locations.last())
            }
        } catch (t: Throwable) {
            Simber.e(t)
            fail(t)
        }
        success()
    }

    private suspend fun saveUserLocation(lastLocation: android.location.Location) {
        val currentSession = eventRepository.getCurrentCaptureSessionEvent()
        currentSession.payload.location =
            Location(lastLocation.latitude, lastLocation.longitude)
        eventRepository.addOrUpdateEvent(currentSession)
        Simber.d("Saving user's location into the current session")
    }
}
