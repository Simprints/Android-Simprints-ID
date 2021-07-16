package com.simprints.fingerprint.activities.connect.issues.otarecovery

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.tools.livedata.postEvent
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.launch

class OtaRecoveryViewModel(private val scannerManager: ScannerManager) : ViewModel() {

    val isConnectionSuccess = MutableLiveData<LiveDataEventWithContent<Boolean>>()

    fun handleTryAgainPressed() {
        viewModelScope.launch {
            try {
                scannerManager.scanner.disconnect()
                scannerManager.scanner.connect()

                isConnectionSuccess.postEvent(true)
            } catch (ex: Throwable) {
                Simber.e(ex)
                isConnectionSuccess.postEvent(false)
            }
        }
    }
}
