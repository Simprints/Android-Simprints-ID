package com.simprints.fingerprint.connect.screens.ota.recovery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.fingerprint.infra.scanner.ScannerManager
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FINGER_CAPTURE
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class OtaRecoveryViewModel @Inject constructor(
    private val scannerManager: ScannerManager,
) : ViewModel() {
    val isConnectionSuccess: LiveData<LiveDataEventWithContent<Boolean>>
        get() = _isConnectionSuccess
    private val _isConnectionSuccess = MutableLiveData<LiveDataEventWithContent<Boolean>>()

    fun handleTryAgainPressed() = viewModelScope.launch {
        try {
            scannerManager.scanner.disconnect()
            scannerManager.scanner.connect()

            _isConnectionSuccess.send(true)
        } catch (ex: Throwable) {
            Simber.e("OTA recovery failed", ex, FINGER_CAPTURE)
            _isConnectionSuccess.send(false)
        }
    }
}
