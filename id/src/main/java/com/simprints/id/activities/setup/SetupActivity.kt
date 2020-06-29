package com.simprints.id.activities.setup

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationRequest
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.BaseSplitActivity
import com.simprints.id.activities.setup.SetupActivity.ViewState.*
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
import com.simprints.id.tools.LocationManager
import com.simprints.id.tools.extensions.hasPermission
import com.simprints.id.tools.extensions.requestPermissionsIfRequired
import kotlinx.android.synthetic.main.activity_setup.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.min

@ExperimentalCoroutinesApi
class SetupActivity: BaseSplitActivity() {

    private lateinit var setupRequest: SetupRequest
    private lateinit var splitInstallManager: SplitInstallManager
    private val viewModel: SetupViewModel by lazy {
        ViewModelProvider(this).get(SetupViewModel::class.java)
    }

    @Inject lateinit var locationManager: LocationManager
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var sessionRepository: SessionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        setupRequest = intent.extras?.getParcelable(CoreResponse.CORE_STEP_BUNDLE) ?: throw InvalidAppRequest()
        splitInstallManager = SplitInstallManagerFactory.create(this)

        viewModel.start(splitInstallManager, getRequiredModules())
        observeViewState()
    }

    private fun getRequiredModules() = setupRequest.modalitiesRequired.map {
        when (it) {
            Modality.FINGER -> getString(R.string.module_feature_finger)
            Modality.FACE -> getString(R.string.module_feature_face)
        }
    }

    private fun observeViewState() {
        viewModel.getViewStateLiveData().observe(this, Observer {
            when(it) {
                StartingDownload -> updateUiForDownloadStarting()
                is RequiresUserConfirmationToDownload -> requestUserConfirmationDoDownloadModalities(it.state)
                is Downloading -> updateUiForDownloadProgress(it.bytesDownloaded, it.totalBytesToDownload)
                ModalitiesInstalling -> updateUiForModalitiesInstalling()
                ModalitiesInstalled -> updateUiForModalitiesInstalledAndAskPermissions()
            }
        })
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

    private fun updateUiForDownloadStarting() {
        with(modalityDownloadText){
            text = getString(R.string.modality_starting_download)
            isVisible = true
        }
        with(modalityDownloadProgressBar) {
            isIndeterminate = true
            isVisible = true
        }
        setupLogo.isVisible = false
    }

    private fun requestUserConfirmationDoDownloadModalities(state: SplitInstallSessionState) {
        splitInstallManager.startConfirmationDialogForResult(state, this, MODALITIES_DOWNLOAD_REQUEST_CODE)
    }

    private fun updateUiForDownloadProgress(bytesDownloaded: Long, totalBytesToDownload: Long) {
        val downloadProgress = calculatePercentage(bytesDownloaded, totalBytesToDownload)
        with(modalityDownloadProgressBar) {
            isVisible = true
            isIndeterminate = false
            progress = downloadProgress
        }
        with(modalityDownloadText) {
            isVisible = true
            text = getString(R.string.modality_downloading).format("$downloadProgress%")
        }
        setupLogo.isVisible = false
    }

    private fun calculatePercentage(progressValue: Long, totalValue: Long) =
        min((100 * (progressValue.toFloat() / totalValue.toFloat())).toInt(), 100)

    private fun updateUiForModalitiesInstalling() {
        with(modalityDownloadText){
            text = getString(R.string.modality_installing)
            isVisible = true
        }
        with(modalityDownloadProgressBar) {
            isIndeterminate = true
            isVisible = true
        }
        setupLogo.isVisible = false
    }

    private fun updateUiForModalitiesInstalledAndAskPermissions() {
        modalityDownloadText.isVisible = false
        modalityDownloadProgressBar.isVisible = false
        setupLogo.isVisible = true
        askPermissionsOrPerformSpecificActions()
    }

    sealed class ViewState {
        object StartingDownload : ViewState()
        class RequiresUserConfirmationToDownload(val state: SplitInstallSessionState): ViewState()
        class Downloading(val bytesDownloaded: Long, val totalBytesToDownload: Long): ViewState()
        object ModalitiesInstalling : ViewState()
        object ModalitiesInstalled : ViewState()
    }

    companion object {
        const val PERMISSIONS_REQUEST_CODE = 99
        const val MODALITIES_DOWNLOAD_REQUEST_CODE = 199
    }
}
