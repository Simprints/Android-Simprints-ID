package com.simprints.feature.login.screens.form

internal sealed class SignInState {
    data object Success : SignInState()

    data object MissingCredential : SignInState()

    data object ProjectIdMismatch : SignInState()

    data class QrCodeValid(
        val projectId: String,
        val projectSecret: String,
    ) : SignInState()

    data object QrNoCameraPermission : SignInState()

    data object QrCameraUnavailable : SignInState()

    data object QrInvalidCode : SignInState()

    data object QrGenericError : SignInState()

    data class ShowUrlChangeDialog(
        val currentUrl: String,
    ) : SignInState()

    data object BadCredentials : SignInState()

    data object Offline : SignInState()

    data object TechnicalFailure : SignInState()

    data object IntegrityException : SignInState()

    data object IntegrityServiceTemporaryDown : SignInState()

    data object MissingOrOutdatedGooglePlayStoreApp : SignInState()

    data class BackendMaintenanceError(
        val estimatedOutage: String? = null,
    ) : SignInState()

    data object Unknown : SignInState()
}
