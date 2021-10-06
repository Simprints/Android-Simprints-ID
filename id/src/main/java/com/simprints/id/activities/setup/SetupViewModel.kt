package com.simprints.id.activities.setup

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.google.android.play.core.ktx.requestProgressFlow
import com.google.android.play.core.ktx.requestSessionStates
import com.google.android.play.core.ktx.status
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.model.SplitInstallErrorCode.NO_ERROR
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus.DOWNLOADED
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus.DOWNLOADING
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus.INSTALLED
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus.INSTALLING
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus.PENDING
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION
import com.simprints.core.analytics.CrashReportTag.ID_SETUP
import com.simprints.id.activities.setup.SetupActivity.ViewState.DeviceOffline
import com.simprints.id.activities.setup.SetupActivity.ViewState.DeviceOnline
import com.simprints.id.activities.setup.SetupActivity.ViewState.Downloading
import com.simprints.id.activities.setup.SetupActivity.ViewState.ModalitiesInstalled
import com.simprints.id.activities.setup.SetupActivity.ViewState.ModalitiesInstalling
import com.simprints.id.activities.setup.SetupActivity.ViewState.RequiresUserConfirmationToDownload
import com.simprints.id.activities.setup.SetupActivity.ViewState.StartingDownload
import com.simprints.id.tools.device.DeviceManager
import com.simprints.id.tools.extensions.trace
import com.simprints.logging.Simber
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class SetupViewModel(
    val deviceManager: DeviceManager
) : ViewModel() {

    internal val scope by lazy { viewModelScope }
    private val modalityDownloadTrace by lazy { trace("modalityDownload") }

    fun getViewStateLiveData(): LiveData<SetupActivity.ViewState> = viewStateLiveData
    fun getDeviceNetworkLiveData(): LiveData<SetupActivity.ViewState> =
        deviceManager.isConnectedLiveData.map {
        if (it) {
            DeviceOnline
        } else {
            DeviceOffline
        }
    }

    private val viewStateLiveData = MutableLiveData<SetupActivity.ViewState>()

    fun start(splitInstallManager: SplitInstallManager, modalitiesRequired: List<String>) {
        val modalitiesToDownload =
            modalitiesRequired.filterNot { splitInstallManager.installedModules.contains(it) }

        val splitInstallRequestBuilder = SplitInstallRequest.newBuilder()

        if (modalitiesToDownload.isNotEmpty()) {
            logMessageForCrashReport("Modalities to download: $modalitiesToDownload")
            modalitiesToDownload.forEach { splitInstallRequestBuilder.addModule(it) }
            startDownloadingModulesAndMonitorProgress(splitInstallManager, splitInstallRequestBuilder.build())
        } else {
            logMessageForCrashReport("Modalities $modalitiesRequired already installed")
            viewStateLiveData.value = ModalitiesInstalled
        }
    }

    private fun startDownloadingModulesAndMonitorProgress(splitInstallManager: SplitInstallManager,
                                                          request: SplitInstallRequest) {
        splitInstallManager.startInstall(request)
            .addOnFailureListener {
                Simber.e(it)
                startDownloadingModulesAndMonitorProgress(splitInstallManager, request)
            }

        monitorDownloadProgress(splitInstallManager)
    }

    @SuppressLint("SwitchIntDef")
    internal fun monitorDownloadProgress(splitInstallManager: SplitInstallManager) {
        viewModelScope.launch {
            splitInstallManager.requestProgressFlow()
                .collect { state ->
                    Simber.d("Setup - Split install state $state")
                    when (state.status()) {
                        DOWNLOADING -> {
                            viewStateLiveData.postValue(Downloading(state.bytesDownloaded(), state.totalBytesToDownload()))
                        }
                        REQUIRES_USER_CONFIRMATION -> {
                            logMessageForCrashReport("Modality download requires user confirmation")
                            viewStateLiveData.postValue(RequiresUserConfirmationToDownload(state))
                        }
                        INSTALLED -> {
                            logMessageForCrashReport("Modalities Installed")
                            modalityDownloadTrace.stop()
                            viewStateLiveData.postValue(ModalitiesInstalled)
                        }
                        INSTALLING -> {
                            logMessageForCrashReport("Installing modalities")
                            viewStateLiveData.postValue(ModalitiesInstalling)
                        }
                        PENDING -> {
                            logMessageForCrashReport("Starting modality download")
                            modalityDownloadTrace.start()
                            viewStateLiveData.postValue(StartingDownload)
                        }
                    }
                }
        }
    }

    private fun logMessageForCrashReport(message: String) {
        Simber.tag(ID_SETUP.name).i(message)
    }
    /**
     * This Method cancels and restart the last installation session if this session is not active
     *
     * @param splitInstallManager
     * @param modalitiesRequired
     */
    fun reStartDownloadIfNecessary(splitInstallManager: SplitInstallManager, modalitiesRequired: List<String>) {
        viewModelScope.launch {
            if (isModalityInstallOnGoing(splitInstallManager) == false) {
                logMessageForCrashReport("Restarting modalities download")
                splitInstallManager.cancelInstall(splitInstallManager.requestSessionStates().last().sessionId())
                start(splitInstallManager, modalitiesRequired)
            }
        }
    }

    /**
     * This method check if the last installation session is active.
     *
     * @param splitInstallManager
     *
     * @return True if the last session has no errors and  is already installed, installing, downloading or downloaded.
     * False otherwise.
     * Null if there is no installation session.
     */
    private suspend fun isModalityInstallOnGoing(splitInstallManager: SplitInstallManager) =
        splitInstallManager.requestSessionStates().lastOrNull()?.let {
            (it.status == INSTALLED || it.status == INSTALLING || it.status == DOWNLOADING || it.status == DOWNLOADED) && it.errorCode() == NO_ERROR
        }
}
