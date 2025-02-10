package com.simprints.feature.login.screens.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DeviceID
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.TimeUtils
import com.simprints.feature.login.LoginParams
import com.simprints.feature.login.screens.qrscanner.QrCodeContent
import com.simprints.feature.login.screens.qrscanner.QrScannerResult
import com.simprints.feature.login.screens.qrscanner.QrScannerResult.QrScannerError
import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.authlogic.model.AuthenticateDataResult
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.LOGIN
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.SimNetwork
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class LoginFormViewModel @Inject constructor(
    @DeviceID private val deviceId: String,
    private val simNetwork: SimNetwork,
    private val authManager: AuthManager,
    private val jsonHelper: JsonHelper,
) : ViewModel() {
    val isProcessingSignIn: LiveData<Boolean>
        get() = _isProcessingSignIn
    private val _isProcessingSignIn = MutableLiveData<Boolean>()
    val signInState: LiveData<LiveDataEventWithContent<SignInState>>
        get() = _signInState
    private val _signInState = MutableLiveData<LiveDataEventWithContent<SignInState>>(null)

    fun signInClicked(
        loginParams: LoginParams,
        projectId: String,
        projectSecret: String,
    ) {
        if (!areMandatoryCredentialsPresent(projectId, projectSecret, loginParams.userId.value)) {
            _signInState.send(SignInState.MissingCredential)
        } else if (projectId != loginParams.projectId) {
            _signInState.send(SignInState.ProjectIdMismatch)
        } else {
            viewModelScope.launch {
                _isProcessingSignIn.value = true
                val result = authManager.authenticateSafely(
                    userId = loginParams.userId.value,
                    projectId = projectId,
                    projectSecret = projectSecret,
                    deviceId = deviceId,
                )
                _signInState.send(mapAuthDataResult(result))
                _isProcessingSignIn.value = false
            }
        }
    }

    private fun mapAuthDataResult(result: AuthenticateDataResult): SignInState = when (result) {
        AuthenticateDataResult.Authenticated -> SignInState.Success
        AuthenticateDataResult.BadCredentials -> SignInState.BadCredentials
        AuthenticateDataResult.IntegrityException -> SignInState.IntegrityException
        AuthenticateDataResult.IntegrityServiceTemporaryDown -> SignInState.IntegrityServiceTemporaryDown
        AuthenticateDataResult.MissingOrOutdatedGooglePlayStoreApp -> SignInState.MissingOrOutdatedGooglePlayStoreApp
        AuthenticateDataResult.Offline -> SignInState.Offline
        AuthenticateDataResult.TechnicalFailure -> SignInState.TechnicalFailure
        AuthenticateDataResult.Unknown -> SignInState.Unknown
        is AuthenticateDataResult.BackendMaintenanceError -> SignInState.BackendMaintenanceError(
            result.estimatedOutage?.let { TimeUtils.getFormattedEstimatedOutage(it) },
        )
    }

    private fun areMandatoryCredentialsPresent(
        projectId: String,
        projectSecret: String,
        userId: String,
    ) = projectId.isNotEmpty() && projectSecret.isNotEmpty() && userId.isNotEmpty()

    fun handleQrResult(
        projectId: String,
        result: QrScannerResult,
    ) {
        if (result.error != null) {
            _signInState.send(mapQrError(result.error))
        } else if (!result.content.isNullOrEmpty()) {
            try {
                val qrContent = jsonHelper.fromJson<QrCodeContent>(result.content)
                Simber.i("QR scanning successful", tag = LOGIN)

                if (projectId != qrContent.projectId) {
                    _signInState.send(SignInState.ProjectIdMismatch)
                } else {
                    qrContent.apiBaseUrl?.let { simNetwork.setApiBaseUrl(it) }
                    _signInState.send(
                        SignInState.QrCodeValid(
                            qrContent.projectId,
                            qrContent.projectSecret,
                        ),
                    )
                }
            } catch (e: Exception) {
                Simber.i("QR scanning unsuccessful", tag = LOGIN)
                _signInState.send(SignInState.QrInvalidCode)
            }
        } else {
            Simber.i("QR code missing", tag = LOGIN)
            _signInState.send(SignInState.QrInvalidCode)
        }
    }

    private fun mapQrError(error: QrScannerError): SignInState = when (error) {
        QrScannerError.NoPermission -> SignInState.QrNoCameraPermission
        QrScannerError.CameraNotAvailable -> SignInState.QrCameraUnavailable
        QrScannerError.UnknownError -> SignInState.QrGenericError
    }

    fun changeUrlClicked() {
        _signInState.send(SignInState.ShowUrlChangeDialog(simNetwork.getApiBaseUrlPrefix()))
    }

    fun saveNewUrl(newUrl: String?) = if (newUrl.isNullOrEmpty()) {
        simNetwork.resetApiBaseUrl()
    } else {
        simNetwork.setApiBaseUrl(newUrl)
    }
}
