package com.simprints.feature.externalcredential.screens.scanqr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.feature.externalcredential.screens.scanqr.usecase.ExternalCredentialQrCodeValidatorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class ExternalCredentialScanQrViewModel @Inject constructor(
    private val externalCredentialQrCodeValidator: ExternalCredentialQrCodeValidatorUseCase
) : ViewModel() {

    private var state: ScanQrState = ScanQrState.NothingScanned
        set(value) {
            field = value
            _stateLiveData.postValue(value)
        }
    private val _stateLiveData = MutableLiveData<ScanQrState>(ScanQrState.NothingScanned)
    val stateLiveData: LiveData<ScanQrState> = _stateLiveData

    private fun updateState(state: (ScanQrState) -> ScanQrState) {
        this.state = state(this.state)
    }

    fun setQrCodeValue(qrCode: String) {
        updateState { ScanQrState.ScanComplete(qrCode) }
    }

    fun isValidQrCodeFormat(qrCodeValue: String): Boolean = externalCredentialQrCodeValidator(qrCodeValue)
}
