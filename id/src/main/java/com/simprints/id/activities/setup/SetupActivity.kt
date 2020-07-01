package com.simprints.id.activities.setup

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.ktx.requestSessionStates
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.BaseSplitActivity
import com.simprints.id.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.id.activities.alert.response.AlertActResponse
import com.simprints.id.activities.alert.response.AlertActResponse.ButtonAction.CLOSE
import com.simprints.id.activities.alert.response.AlertActResponse.ButtonAction.TRY_AGAIN
import com.simprints.id.activities.setup.SetupActivity.ViewState.*
import com.simprints.id.activities.setup.SetupActivityHelper.extractPermissionsFromRequest
import com.simprints.id.activities.setup.SetupActivityHelper.storeUserLocationIntoCurrentSession
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.core.requests.SetupRequest
import com.simprints.id.domain.moduleapi.core.response.SetupResponse
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.orchestrator.steps.core.response.CoreResponse
import com.simprints.id.tools.InternalConstants
import com.simprints.id.tools.LocationManager
import com.simprints.id.tools.extensions.hasPermission
import com.simprints.id.tools.extensions.requestPermissionsIfRequired
import kotlinx.android.synthetic.main.activity_setup.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.min

@ExperimentalCoroutinesApi
class SetupActivity: BaseSplitActivity() {

    @Inject lateinit var locationManager: LocationManager
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var sessionRepository: SessionRepository
    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper
    @Inject lateinit var viewModelFactory: SetupViewModelFactory

    private lateinit var setupRequest: SetupRequest
    private lateinit var splitInstallManager: SplitInstallManager
    private val viewModel: SetupViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(SetupViewModel::class.java)
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
        observeNetworkState()
    }

    private fun injectDependencies() {
        val component = (application as Application).component
        component.inject(this)
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
                is Downloading -> {
                    updateUiForDownloadProgress(it.bytesDownloaded, it.totalBytesToDownload)
                    monitorInactivityWhileDownloading(it.bytesDownloaded)
                }
                ModalitiesInstalling -> updateUiForModalitiesInstalling()
                ModalitiesInstalled -> updateUiForModalitiesInstalledAndAskPermissions()
            }
        })
    }

    private fun observeNetworkState() {
        viewModel.getDeviceNetworkLiveData().observe(this, Observer {
            Timber.d("Setup - Observing network $it")
            if (it == DeviceOffline && viewModel.getViewStateLiveData().value != ModalitiesInstalled) {
                launchAlertIfNecessary()
            }
        })
    }

    private fun launchAlertIfNecessary() {
        lifecycleScope.launchWhenResumed {
            if(splitInstallManager.requestSessionStates().last().status() != REQUIRES_USER_CONFIRMATION) {
                launchAlert(this@SetupActivity, AlertType.OFFLINE_DURING_SETUP)
            }
        }
    }

    private fun monitorInactivityWhileDownloading(lastDownloadedBytes: Long) {
        val handler = Handler()
        handler.postDelayed({
            viewModel.getViewStateLiveData().value?.let {
                if (it is Downloading && it.bytesDownloaded == lastDownloadedBytes) {
                    updateUiForDownloadTakingLonger()
                }
            }
        }, SLOW_DOWNLOAD_DELAY_THRESHOLD)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    storeUserLocationIntoCurrentSession(locationManager, sessionRepository, crashReportManager)
                }
            }
        }
        setResultAndFinish()
    }

    private fun askPermissionsOrPerformSpecificActions() {
        val permissions = extractPermissionsFromRequest(setupRequest)

        if (permissions.all { hasPermission(it) }) {
            performPermissionActionsAndFinish()
        } else {
            requestPermissionsIfRequired(permissions, PERMISSIONS_REQUEST_CODE)
        }
    }

    private fun performPermissionActionsAndFinish() {
        storeUserLocationIntoCurrentSession(locationManager, sessionRepository, crashReportManager)
        setResultAndFinish()
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

    private fun updateUiForDownloadTakingLonger() {
        with(modalityDownloadText){
            text = androidResourcesHelper.getString(R.string.modality_download_taking_longer)
            isVisible = true
        }
        with(modalityDownloadProgressBar) {
            isIndeterminate = true
            isVisible = true
        }
        setupLogo.isVisible = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            MODALITIES_DOWNLOAD_REQUEST_CODE -> handleModalityDownloadResult(resultCode)
            InternalConstants.RequestIntents.ALERT_ACTIVITY_REQUEST -> handleAlertResponse(data)
        }
    }

    private fun handleModalityDownloadResult(resultCode: Int) {
        if(resultCode == Activity.RESULT_CANCELED) {
            launchAlert(this, AlertType.SETUP_MODALITY_DOWNLOAD_CANCELLED)
        }
    }

    private fun handleAlertResponse(data: Intent?) {
        tryToGetAlertActResponseAndHandleAction(data) ?: finishAffinity()
    }

    private fun tryToGetAlertActResponseAndHandleAction(data: Intent?) =
        data?.getParcelableExtra<AlertActResponse>(AlertActResponse.BUNDLE_KEY)?.let {
            when(it.buttonAction) {
                CLOSE -> finishAffinity()
                TRY_AGAIN -> viewModel.reStartDownloadIfNecessary(splitInstallManager, getRequiredModules())
            }
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
        object DeviceOnline: ViewState()
        object DeviceOffline: ViewState()
    }

    companion object {
        const val PERMISSIONS_REQUEST_CODE = 99
        const val MODALITIES_DOWNLOAD_REQUEST_CODE = 199
        const val SLOW_DOWNLOAD_DELAY_THRESHOLD = 30000L
    }
}
