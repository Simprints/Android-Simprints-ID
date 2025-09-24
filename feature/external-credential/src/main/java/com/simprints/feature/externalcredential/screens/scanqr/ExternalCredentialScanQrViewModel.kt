package com.simprints.feature.externalcredential.screens.scanqr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.domain.permission.PermissionStatus
import com.simprints.feature.externalcredential.screens.scanqr.usecase.ExternalCredentialQrCodeValidatorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class ExternalCredentialScanQrViewModel @Inject constructor(
    private val externalCredentialQrCodeValidator: ExternalCredentialQrCodeValidatorUseCase
) : ViewModel() {

    private var state: ScanQrState = ScanQrState.ReadyToScan
        set(value) {
            field = value
            _stateLiveData.postValue(value)
        }
    private val _stateLiveData = MutableLiveData<ScanQrState>(ScanQrState.ReadyToScan)
    val stateLiveData: LiveData<ScanQrState> = _stateLiveData

    private fun updateState(state: (ScanQrState) -> ScanQrState) {
        this.state = state(this.state)
    }

    fun updateCapturedValue(value: String?) {
        val newState = when (value) {
            null -> ScanQrState.ReadyToScan
            else -> ScanQrState.QrCodeCaptured(value)
        }
        updateState { newState }
    }

    fun updateCameraPermissionStatus(permissionStatus: PermissionStatus) {
        val newState = when (permissionStatus) {
            PermissionStatus.Granted -> ScanQrState.ReadyToScan
            PermissionStatus.Denied -> ScanQrState.NoCameraPermission(shouldOpenPhoneSettings = false)
            PermissionStatus.DeniedNeverAskAgain -> ScanQrState.NoCameraPermission(shouldOpenPhoneSettings = true)
        }
        updateState { newState }
    }

    fun isValidQrCodeFormat(qrCodeValue: String): Boolean = externalCredentialQrCodeValidator(qrCodeValue)
}
