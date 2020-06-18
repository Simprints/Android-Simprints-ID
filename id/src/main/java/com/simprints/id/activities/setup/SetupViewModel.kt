package com.simprints.id.activities.setup

import android.annotation.SuppressLint
import androidx.lifecycle.*
import com.google.android.play.core.ktx.requestProgressFlow
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.simprints.id.activities.setup.SetupActivity.ViewState.*
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTag.ID_SETUP
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger.NETWORK
import com.simprints.id.tools.device.DeviceManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

@ExperimentalCoroutinesApi
class SetupViewModel(private val deviceManager: DeviceManager,
                     private val crashReportManager: CrashReportManager) : ViewModel() {

    fun getViewStateLiveData(): LiveData<SetupActivity.ViewState> = viewStateLiveData
    fun getDeviceNetworkLiveData(): LiveData<SetupActivity.ViewState> = deviceNetwork

    private lateinit var deviceNetwork: LiveData<SetupActivity.ViewState>
    private val viewStateLiveData = MutableLiveData<SetupActivity.ViewState>()

    fun start(splitInstallManager: SplitInstallManager, modalitiesRequired: List<String>) {
        val modalitiesToDownload =
            modalitiesRequired.filterNot { splitInstallManager.installedModules.contains(it) }

        val splitInstallRequestBuilder = SplitInstallRequest.newBuilder()

        if (modalitiesToDownload.isNotEmpty()) {
            logMessageForCrashReport("Modalities to download: $modalitiesToDownload")
            modalitiesToDownload.forEach { splitInstallRequestBuilder.addModule(it) }
            splitInstallManager.startInstall(splitInstallRequestBuilder.build())
            monitorDownloadProgress(splitInstallManager)
            monitorNetworkState()
        } else {
            logMessageForCrashReport("Modalities $modalitiesRequired already installed")
            viewStateLiveData.value = ModalitiesInstalled
        }
    }

    @SuppressLint("SwitchIntDef")
    internal fun monitorDownloadProgress(splitInstallManager: SplitInstallManager) {
        viewModelScope.launch {
            splitInstallManager.requestProgressFlow()
                .collect { state ->
                    Timber.d("Split install state $state")
                    when (state.status()) {
                        SplitInstallSessionStatus.DOWNLOADING -> {
                            viewStateLiveData.postValue(Downloading(state.bytesDownloaded(), state.totalBytesToDownload()))
                        }
                        SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                            logMessageForCrashReport("Modality download requires user confirmation")
                            viewStateLiveData.postValue(RequiresUserConfirmationToDownload(state))
                        }
                        SplitInstallSessionStatus.INSTALLED -> {
                            logMessageForCrashReport("Modalities Installed")
                            viewStateLiveData.postValue(ModalitiesInstalled)
                        }
                        SplitInstallSessionStatus.INSTALLING -> {
                            logMessageForCrashReport("Installing modalities")
                            viewStateLiveData.postValue(ModalitiesInstalling)
                        }
                        SplitInstallSessionStatus.CANCELED -> {
                            
                        }
                        SplitInstallSessionStatus.FAILED -> {
                            Timber.d("Split install fail: ${state.errorCode()}")
                        }
                        SplitInstallSessionStatus.PENDING -> {
                            logMessageForCrashReport("Starting modality download")
                            viewStateLiveData.postValue(StartingDownload)
                        }
                    }
                }
        }
    }

    private fun monitorNetworkState() {
        deviceNetwork = deviceManager.isConnectedLiveData.map {
            if (it) {
                DeviceOnline
            } else {
                DeviceOffline
            }
        }
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(ID_SETUP, NETWORK, message = message)
    }
}
