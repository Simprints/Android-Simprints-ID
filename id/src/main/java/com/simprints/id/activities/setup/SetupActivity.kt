package com.simprints.id.activities.setup

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.ktx.requestSessionStates
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.extentions.inBackground
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.id.activities.alert.response.AlertActResponse
import com.simprints.id.activities.alert.response.AlertActResponse.ButtonAction.CLOSE
import com.simprints.id.activities.alert.response.AlertActResponse.ButtonAction.TRY_AGAIN
import com.simprints.id.activities.setup.SetupActivity.ViewState.*
import com.simprints.id.activities.setup.SetupActivityHelper.extractPermissionsFromRequest
import com.simprints.id.activities.setup.SetupActivityHelper.storeUserLocationIntoCurrentSession
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.databinding.ActivitySetupBinding
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.modality.Modality
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.orchestrator.steps.core.requests.SetupRequest
import com.simprints.id.orchestrator.steps.core.response.CoreResponse
import com.simprints.id.orchestrator.steps.core.response.SetupResponse
import com.simprints.id.tools.InternalConstants
import com.simprints.id.tools.LocationManager
import com.simprints.id.tools.extensions.hasPermission
import com.simprints.id.tools.extensions.requestPermissionsIfRequired
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule
import kotlin.math.min

@ExperimentalCoroutinesApi
class SetupActivity : BaseSplitActivity() {

    @Inject lateinit var locationManager: LocationManager
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var viewModelFactory: SetupViewModelFactory
    @Inject lateinit var eventRepository: EventRepository

    private lateinit var setupRequest: SetupRequest
    private lateinit var splitInstallManager: SplitInstallManager
    private val viewModel: SetupViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(SetupViewModel::class.java)
    }
    private val binding by viewBinding(ActivitySetupBinding::inflate)

    private var slowDownloadTimer: TimerTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupRequest = intent.extras?.getParcelable(CoreResponse.CORE_STEP_BUNDLE)
            ?: throw InvalidAppRequest()
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
            when (it) {
                StartingDownload -> updateUiForDownloadStarting()
                is RequiresUserConfirmationToDownload -> requestUserConfirmationDoDownloadModalities(it.state)
                is Downloading -> {
                    updateUiForDownloadProgress(it.bytesDownloaded, it.totalBytesToDownload)
                    rescheduleTimerForSlowDownloadUI()
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
            if (splitInstallManager.requestSessionStates().lastOrNull()?.status() != REQUIRES_USER_CONFIRMATION) {
                launchAlert(this@SetupActivity, AlertType.OFFLINE_DURING_SETUP)
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    collectLocationInBackground()
                }
            }
        }
        setResultAndFinish(SETUP_COMPLETE_FLAG)
    }

    private fun collectLocationInBackground() {
        inBackground(Dispatchers.Main) {
            try {
                storeUserLocationIntoCurrentSession(locationManager, eventRepository, crashReportManager)
            } catch (t: Throwable) {
                Timber.d(t)
            }
        }
    }

    private fun askPermissionsOrPerformSpecificActions() {
        val permissions = extractPermissionsFromRequest(setupRequest)

        if (permissions.all { hasPermission(it) }) {
            Timber.d("All permissions are granted")
            performPermissionActionsAndFinish()
        } else {
            requestPermissionsIfRequired(permissions, PERMISSIONS_REQUEST_CODE)
        }
    }

    private fun performPermissionActionsAndFinish() {
        lifecycleScope.launch {
            Timber.d("Adding location to session")
            collectLocationInBackground()
            setResultAndFinish(SETUP_COMPLETE_FLAG)
        }
    }

    private fun setResultAndFinish(setupCompleteFlag: Boolean) {
        setResult(Activity.RESULT_OK, buildIntentForResponse(setupCompleteFlag))
        finish()
    }

    private fun buildIntentForResponse(setupComplete: Boolean) = Intent().apply {
        putExtra(CoreResponse.CORE_STEP_BUNDLE, SetupResponse(setupComplete))
    }

    private fun updateUiForDownloadStarting() {
        with(binding.modalityDownloadText) {
            text = getString(R.string.modality_starting_download)
            isVisible = true
        }
        with(binding.modalityDownloadProgressBar) {
            isIndeterminate = true
            isVisible = true

        }
        binding.setupLogo.isVisible = false
    }

    private fun requestUserConfirmationDoDownloadModalities(state: SplitInstallSessionState) {
        splitInstallManager.startConfirmationDialogForResult(state, this, MODALITIES_DOWNLOAD_REQUEST_CODE)
    }

    private fun updateUiForDownloadProgress(bytesDownloaded: Long, totalBytesToDownload: Long) {
        val downloadProgress = calculatePercentage(bytesDownloaded, totalBytesToDownload)
        with(binding.modalityDownloadProgressBar) {
            isVisible = true
            isIndeterminate = false
            progress = downloadProgress
        }
        with(binding.modalityDownloadText) {
            isVisible = true
            text = getString(R.string.modality_downloading).format("$downloadProgress%")
        }
        binding.setupLogo.isVisible = false
    }

    private fun rescheduleTimerForSlowDownloadUI() {
        slowDownloadTimer?.cancel()
        slowDownloadTimer = Timer().schedule(SLOW_DOWNLOAD_DELAY_THRESHOLD) {
            runOnUiThread { updateUiForDownloadTakingLonger() }
        }
    }

    private fun updateUiForDownloadTakingLonger() {
        with(binding.modalityDownloadText) {
            text = getString(R.string.modality_download_taking_longer)
            isVisible = true
        }
        with(binding.modalityDownloadProgressBar) {
            isIndeterminate = true
            isVisible = true
        }
        binding.setupLogo.isVisible = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            MODALITIES_DOWNLOAD_REQUEST_CODE -> handleModalityDownloadResult(resultCode)
            InternalConstants.RequestIntents.ALERT_ACTIVITY_REQUEST -> handleAlertResponse(data)
        }
    }

    private fun handleModalityDownloadResult(resultCode: Int) {
        if (resultCode == Activity.RESULT_CANCELED) {
            launchAlert(this, AlertType.SETUP_MODALITY_DOWNLOAD_CANCELLED)
        }
    }

    private fun handleAlertResponse(data: Intent?) {
        data?.getParcelableExtra<AlertActResponse>(AlertActResponse.BUNDLE_KEY)?.let {
            when (it.buttonAction) {
                CLOSE -> setResultAndFinish(SETUP_NOT_COMPLETE_FLAG)
                TRY_AGAIN -> viewModel.reStartDownloadIfNecessary(splitInstallManager, getRequiredModules())
            }
        } ?: setResultAndFinish(SETUP_NOT_COMPLETE_FLAG)
    }

    private fun calculatePercentage(progressValue: Long, totalValue: Long) =
        min((100 * (progressValue.toFloat() / totalValue.toFloat())).toInt(), 100)

    private fun updateUiForModalitiesInstalling() {
        with(binding.modalityDownloadText) {
            text = getString(R.string.modality_installing)
            isVisible = true
        }
        with(binding.modalityDownloadProgressBar) {
            isIndeterminate = true
            isVisible = true
        }
        binding.setupLogo.isVisible = false
    }

    private fun updateUiForModalitiesInstalledAndAskPermissions() {
        binding.modalityDownloadText.isVisible = false
        binding.modalityDownloadProgressBar.isVisible = false
        binding.setupLogo.isVisible = true
        askPermissionsOrPerformSpecificActions()
    }

    override fun onBackPressed() {
        setResultAndFinish(SETUP_NOT_COMPLETE_FLAG)
    }

    sealed class ViewState {
        object StartingDownload : ViewState()
        class RequiresUserConfirmationToDownload(val state: SplitInstallSessionState) : ViewState()
        class Downloading(val bytesDownloaded: Long, val totalBytesToDownload: Long) : ViewState()
        object ModalitiesInstalling : ViewState()
        object ModalitiesInstalled : ViewState()
        object DeviceOnline : ViewState()
        object DeviceOffline : ViewState()
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 99
        private const val MODALITIES_DOWNLOAD_REQUEST_CODE = 199
        private const val SLOW_DOWNLOAD_DELAY_THRESHOLD = 15000L
        private const val SETUP_COMPLETE_FLAG = true
        private const val SETUP_NOT_COMPLETE_FLAG = false
    }
}
