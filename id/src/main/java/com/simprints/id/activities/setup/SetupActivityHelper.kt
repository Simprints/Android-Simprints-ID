package com.simprints.id.activities.setup

import com.google.android.gms.location.LocationRequest
import com.simprints.core.tools.extentions.inBackground
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.session.Location
import com.simprints.id.di.AppComponent
import com.simprints.id.tools.LocationManager
import com.simprints.logging.Simber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import javax.inject.Inject

class SetupActivityHelper private constructor() {
    @Inject
    lateinit var eventRepository: EventRepository

    @Inject
    lateinit var locationManager: LocationManager

    companion object {
        private var instance: SetupActivityHelper? = null
        fun getInstance(component: AppComponent): SetupActivityHelper =
            instance ?: SetupActivityHelper().also { component.inject(it) }

        fun clearInstance() {
            instance = null
        }
    }

    internal  fun storeUserLocationIntoCurrentSession() {
        inBackground(Dispatchers.Main) {
            try {
                val locationRequest = LocationRequest().apply {
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }
                val locationsFlow = locationManager.requestLocation(locationRequest).take(1)
                locationsFlow.collect { locations ->
                    val lastLocation = locations.last()
                    val currentSession = eventRepository.getCurrentCaptureSessionEvent()
                    currentSession.payload.location =
                        Location(lastLocation.latitude, lastLocation.longitude)
                    eventRepository.addOrUpdateEvent(currentSession)
                    Simber.d("Saving user's location into the current session")
                    clearInstance()
                }
            } catch (t: Throwable) {
                Simber.e(t)
                clearInstance()
            }
        }
    }
}
