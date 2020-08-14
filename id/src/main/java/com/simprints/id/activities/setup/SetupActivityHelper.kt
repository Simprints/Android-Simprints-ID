package com.simprints.id.activities.setup

import android.Manifest
import com.google.android.gms.location.LocationRequest
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.models.session.Location
import com.simprints.id.exceptions.safe.FailedToRetrieveUserLocation
import com.simprints.id.orchestrator.steps.core.requests.SetupPermission
import com.simprints.id.orchestrator.steps.core.requests.SetupRequest
import com.simprints.id.tools.LocationManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import timber.log.Timber

object SetupActivityHelper {

    internal fun extractPermissionsFromRequest(setupRequest: SetupRequest): List<String> =
        setupRequest.requiredPermissions.map {
            when (it) {
                SetupPermission.LOCATION -> Manifest.permission.ACCESS_FINE_LOCATION
            }
        }

    internal suspend fun storeUserLocationIntoCurrentSession(locationManager: LocationManager,
                                                             eventRepository: EventRepository,
                                                             crashReportManager: CrashReportManager) {
        try {
            val locationRequest = LocationRequest().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            val locationsFlow = locationManager.requestLocation(locationRequest).take(1)
            locationsFlow.collect { locations ->
                val lastLocation = locations.last()
                eventRepository.updateCurrentSession {
                    Timber.d("Saving user's location into the current session")
                    it.payload.location = Location(lastLocation.latitude, lastLocation.longitude)
                }
            }
        } catch (t: Throwable) {
            crashReportManager.logExceptionOrSafeException(FailedToRetrieveUserLocation(t))
        }
    }
}
