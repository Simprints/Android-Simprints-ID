package com.simprints.id.activities.setup

import android.annotation.SuppressLint
import androidx.lifecycle.*
import com.google.android.play.core.ktx.requestProgressFlow
import com.google.android.play.core.ktx.requestSessionStates
import com.google.android.play.core.ktx.status
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.model.SplitInstallErrorCode.NO_ERROR
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus.*
import com.simprints.id.activities.setup.SetupActivity.ViewState.*
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag.ID_SETUP
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger.NETWORK
import com.simprints.id.tools.device.DeviceManager
import com.simprints.id.tools.extensions.trace
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

@ExperimentalCoroutinesApi
class SetupViewModel(val deviceManager: DeviceManager,
                     private val crashReportManager: CrashReportManager) : ViewModel() {

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
                Timber.e(it)
                startDownloadingModulesAndMonitorProgress(splitInstallManager, request)
            }

        monitorDownloadProgress(splitInstallManager)
    }

    @SuppressLint("SwitchIntDef")
    internal fun monitorDownloadProgress(splitInstallManager: SplitInstallManager) {
        viewModelScope.launch {
            splitInstallManager.requestProgressFlow()
                .collect { state ->
                    Timber.d("Setup - Split install state $state")
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
        crashReportManager.logMessageForCrashReport(ID_SETUP, NETWORK, message = message)
    }

    fun reStartDownloadIfNecessary(splitInstallManager: SplitInstallManager, modalitiesRequired: List<String>) {
        viewModelScope.launch {
            if (!isModalityInstallOnGoing(splitInstallManager)) {
                logMessageForCrashReport("Restarting modalities download")
                splitInstallManager.cancelInstall(splitInstallManager.requestSessionStates().last().sessionId())
                start(splitInstallManager, modalitiesRequired)
            }
        }
    }

    private suspend fun isModalityInstallOnGoing(splitInstallManager: SplitInstallManager) =
        splitInstallManager.requestSessionStates().last().let {
            (it.status == INSTALLED || it.status == INSTALLING || it.status == DOWNLOADING || it.status == DOWNLOADED) && it.errorCode() == NO_ERROR
        }
}
