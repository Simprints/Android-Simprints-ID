package com.simprints.id.activities.setup

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.play.core.ktx.requestProgressFlow
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.simprints.id.activities.enrollast.EnrolLastBiometricsActivity
import com.simprints.id.activities.setup.SetupActivity.ViewState.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class SetupViewModel : ViewModel() {

    fun getViewStateLiveData(): LiveData<SetupActivity.ViewState> = viewStateLiveData
    private val viewStateLiveData = MutableLiveData<SetupActivity.ViewState>()

    fun start(splitInstallManager: SplitInstallManager, modalitiesRequired: List<String>) {
        val modalitiesToDownload =
            modalitiesRequired.filterNot { splitInstallManager.installedModules.contains(it) }

        val splitInstallRequestBuilder = SplitInstallRequest.newBuilder()

        if (modalitiesToDownload.isNotEmpty()) {
            modalitiesToDownload.forEach { splitInstallRequestBuilder.addModule(it) }
            splitInstallManager.startInstall(splitInstallRequestBuilder.build())
            monitorDownloadProgress(splitInstallManager)
        } else {
            viewStateLiveData.value = ModalitiesInstalled
        }
    }

    @SuppressLint("SwitchIntDef")
    private fun monitorDownloadProgress(splitInstallManager: SplitInstallManager) {
        viewModelScope.launch {
            splitInstallManager.requestProgressFlow()
                .collect { state ->
                    when (state.status()) {
                        SplitInstallSessionStatus.DOWNLOADING -> {
                            viewStateLiveData.postValue(Downloading(state.bytesDownloaded(), state.totalBytesToDownload()))
                        }
                        SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                            viewStateLiveData.postValue(RequiresUserConfirmationToDownload(state))
                        }
                        SplitInstallSessionStatus.INSTALLED -> {
                            viewStateLiveData.postValue(ModalitiesInstalled)
                        }
                        SplitInstallSessionStatus.INSTALLING -> {
                            viewStateLiveData.postValue(ModalitiesInstalling)
                        }
                        SplitInstallSessionStatus.FAILED -> {
                            TODO("Show alert or retry based on error code. Will be done in the next story")
                        }
                        SplitInstallSessionStatus.PENDING -> {
                            viewStateLiveData.postValue(StartingDownload)
                        }
                    }
                }
        }
    }
}
