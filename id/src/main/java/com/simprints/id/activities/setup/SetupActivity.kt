package com.simprints.id.activities.setup

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.view.isVisible
import com.google.android.gms.location.LocationRequest
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.BaseSplitActivity
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.domain.models.session.Location
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.core.requests.SetupPermission
import com.simprints.id.domain.moduleapi.core.requests.SetupRequest
import com.simprints.id.domain.moduleapi.core.response.SetupResponse
import com.simprints.id.exceptions.safe.FailedToRetrieveUserLocation
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.orchestrator.steps.core.response.CoreResponse
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.LocationManager
import com.simprints.id.tools.extensions.hasPermission
import com.simprints.id.tools.extensions.requestPermissionsIfRequired
import kotlinx.android.synthetic.main.activity_setup.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.min

class SetupActivity: BaseSplitActivity() {

    private lateinit var setupRequest: SetupRequest
    private lateinit var splitInstallManager: SplitInstallManager

    @Inject lateinit var locationManager: LocationManager
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var sessionRepository: SessionRepository
    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper

    @SuppressLint("SwitchIntDef")
    private val listener = SplitInstallStateUpdatedListener { state ->
        Timber.d("Setup -- $state")
        when (state.status()) {
            SplitInstallSessionStatus.DOWNLOADING -> {
                setDownloadProgressInUi(state.bytesDownloaded(), state.totalBytesToDownload())
            }
            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                splitInstallManager.startConfirmationDialogForResult(state, this, MODALITIES_DOWNLOAD_REQUEST_CODE)
            }
            SplitInstallSessionStatus.INSTALLED -> {
                setUiForModalitiesInstalled()
                askPermissionsOrPerformSpecificActions()
            }
            SplitInstallSessionStatus.INSTALLING -> {
                setUiForModalitiesInstallingOrPending()
            }
            SplitInstallSessionStatus.FAILED -> {

            }
            SplitInstallSessionStatus.PENDING -> {
                setUiForModalitiesInstallingOrPending()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        modalityDownloadText.text = androidResourcesHelper.getString(R.string.modality_downloading)

        setupRequest = intent.extras?.getParcelable(CoreResponse.CORE_STEP_BUNDLE) ?: throw InvalidAppRequest()
        splitInstallManager = SplitInstallManagerFactory.create(this)

        downloadAndInstallRequiredModules()
    }

    private fun downloadAndInstallRequiredModules() {
        val modalitiesToDownload = setupRequest.modalitiesRequired.map {
            when (it) {
                Modality.FINGER -> getString(R.string.module_feature_finger)
                Modality.FACE -> getString(R.string.module_feature_face)
            }
        }.filterNot { splitInstallManager.installedModules.contains(it) }

        val splitInstallRequestBuilder = SplitInstallRequest.newBuilder()

        if (modalitiesToDownload.isNotEmpty()) {
            modalitiesToDownload.forEach { splitInstallRequestBuilder.addModule(it) }
            splitInstallManager.startInstall(splitInstallRequestBuilder.build())
        } else {
            askPermissionsOrPerformSpecificActions()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    storeUserLocationIntoCurrentSession()
                }
            }
        }
        setResultAndFinish()
    }

    private fun injectDependencies() {
        val component = (application as Application).component
        component.inject(this)
    }

    private fun askPermissionsOrPerformSpecificActions() {
        val permissions = extractPermissionsFromRequest()

        if (permissions.all { hasPermission(it) }) {
            performPermissionActionsAndFinish()
        } else {
            requestPermissionsIfRequired(permissions, PERMISSIONS_REQUEST_CODE)
        }
    }

    private fun extractPermissionsFromRequest(): List<String> = setupRequest.requiredPermissions.map {
        when (it) {
            SetupPermission.LOCATION -> Manifest.permission.ACCESS_FINE_LOCATION
        }
    }

    private fun performPermissionActionsAndFinish() {
        storeUserLocationIntoCurrentSession()
        setResultAndFinish()
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

    private fun setDownloadProgressInUi(bytesDownloaded: Long, totalBytesToDownload: Long) {
        val downloadProgress = calculatePercentage(bytesDownloaded, totalBytesToDownload)
        with(modalityDownloadProgressBar) {
            isVisible = true
            isIndeterminate = false
            progress = downloadProgress
        }
        modalityDownloadText.isVisible = true
        setupLogo.isVisible = false
    }

    private fun calculatePercentage(progressValue: Long, totalValue: Long) =
        min((100 * (progressValue.toFloat() / totalValue.toFloat())).toInt(), 100)

    private fun setUiForModalitiesInstallingOrPending() {
        modalityDownloadText.isVisible = true
        with(modalityDownloadProgressBar) {
            isIndeterminate = true
            isVisible = true
        }
        setupLogo.isVisible = false
    }

    private fun setUiForModalitiesInstalled() {
        modalityDownloadText.isVisible = false
        modalityDownloadProgressBar.isVisible = false
        setupLogo.isVisible = true
    }

    override fun onResume() {
        splitInstallManager.registerListener(listener)
        super.onResume()
    }

    override fun onPause() {
        splitInstallManager.unregisterListener(listener)
        super.onPause()
    }

    companion object {
        const val PERMISSIONS_REQUEST_CODE = 99
        const val MODALITIES_DOWNLOAD_REQUEST_CODE = 199
    }
}
