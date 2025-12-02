package com.simprints.feature.externalcredential.screens.scanqr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.domain.permission.PermissionStatus
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.screens.scanqr.usecase.ExternalCredentialQrCodeValidatorUseCase
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.config.sync.ConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ExternalCredentialScanQrViewModel @Inject constructor(
    private val timeHelper: TimeHelper,
    private val externalCredentialQrCodeValidator: ExternalCredentialQrCodeValidatorUseCase,
    private val configManager: ConfigManager,
    private val tokenizationProcessor: TokenizationProcessor,
) : ViewModel() {
    private lateinit var startTime: Timestamp
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
        viewModelScope.launch {
            val newState = when (value) {
                null -> ScanQrState.ReadyToScan
                else -> configManager.getProject()?.let { project ->
                    val qrCodeEncrypted = tokenizationProcessor.encrypt(
                        decrypted = value.asTokenizableRaw(),
                        tokenKeyType = TokenKeyType.ExternalCredential,
                        project = project,
                    ) as TokenizableString.Tokenized
                    ScanQrState.QrCodeCaptured(
                        scanStartTime = startTime,
                        scanEndTime = timeHelper.now(),
                        qrCode = value.asTokenizableRaw(),
                        qrCodeEncrypted = qrCodeEncrypted,
                    )
                } ?: ScanQrState.ReadyToScan
            }
            updateState { newState }
        }
    }

    fun updateCameraPermissionStatus(permissionStatus: PermissionStatus) {
        val newState = when (permissionStatus) {
            PermissionStatus.Granted -> {
                startTime = timeHelper.now() // Reset scan timer
                ScanQrState.ReadyToScan
            }
            PermissionStatus.Denied -> ScanQrState.NoCameraPermission(shouldOpenPhoneSettings = false)
            PermissionStatus.DeniedNeverAskAgain -> ScanQrState.NoCameraPermission(shouldOpenPhoneSettings = true)
        }
        updateState { newState }
    }

    fun isValidQrCodeFormat(qrCodeValue: TokenizableString.Raw): Boolean = externalCredentialQrCodeValidator(qrCodeValue.value)
}
