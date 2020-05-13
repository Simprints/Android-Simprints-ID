package com.simprints.id.activities.setup

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.LocationRequest
import com.simprints.id.Application
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.domain.models.session.Location
import com.simprints.id.domain.moduleapi.core.response.CoreResponse
import com.simprints.id.domain.moduleapi.core.response.SetupResponse
import com.simprints.id.exceptions.safe.FailedToRetrieveUserLocation
import com.simprints.id.tools.LocationManager
import com.simprints.id.tools.extensions.requestPermissionsIfRequired
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SetupActivity: AppCompatActivity() {

    @Inject lateinit var locationManager: LocationManager
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var sessionRepository: SessionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        super.onCreate(savedInstanceState)

        askLocationPermissionIfRequired()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    storeUserLocationIntoCurrentSession()
                }
                setResultAndFinish()
            }
        }
    }

    private fun injectDependencies() {
        val component = (application as Application).component
        component.inject(this)
    }

    private fun askLocationPermissionIfRequired() {
        requestPermissionsIfRequired(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE)
    }

    private fun storeUserLocationIntoCurrentSession() {
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

    private fun setResultAndFinish() {
        setResult(Activity.RESULT_OK, buildIntentForResponse())
        finish()
    }

    private fun buildIntentForResponse() = Intent().apply {
        putExtra(CoreResponse.CORE_STEP_BUNDLE, SetupResponse())
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 99
    }
}
