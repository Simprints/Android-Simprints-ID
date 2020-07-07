package com.simprints.id.activities.setup

import android.Manifest
import com.google.android.gms.location.LocationRequest
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.domain.models.session.Location
import com.simprints.id.orchestrator.steps.core.requests.SetupPermission
import com.simprints.id.orchestrator.steps.core.requests.SetupRequest
import com.simprints.id.exceptions.safe.FailedToRetrieveUserLocation
import com.simprints.id.tools.LocationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

object SetupActivityHelper {

    internal fun extractPermissionsFromRequest(setupRequest: SetupRequest): List<String> =
        setupRequest.requiredPermissions.map {
            when (it) {
                SetupPermission.LOCATION -> Manifest.permission.ACCESS_FINE_LOCATION
            }
        }

    internal fun storeUserLocationIntoCurrentSession(locationManager: LocationManager,
                                                    sessionRepository: SessionRepository,
                                                    crashReportManager: CrashReportManager) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val locationRequest = LocationRequest().apply {
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }
                val locationsFlow = locationManager.requestLocation(locationRequest).take(1)
                locationsFlow.collect { locations ->
                    val lastLocation = locations.last()
                    sessionRepository.updateCurrentSession {
                        Timber.d("Saving user's location into the current session")
                        it.location = Location(lastLocation.latitude, lastLocation.longitude)
                    }
                }
            } catch (t: Throwable) {
                crashReportManager.logExceptionOrSafeException(FailedToRetrieveUserLocation(t))
            }
        }
    }
}
