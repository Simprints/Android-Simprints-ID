package com.simprints.feature.login.screens.form

internal sealed class SignInState {

    object Success : SignInState()

    object MissingCredential : SignInState()
    object ProjectIdMismatch : SignInState()
    object BadCredentials : SignInState()
    object Offline : SignInState()
    object TechnicalFailure : SignInState()
    object IntegrityException : SignInState()
    object IntegrityServiceTemporaryDown : SignInState()
    object MissingOrOutdatedGooglePlayStoreApp : SignInState()
    data class BackendMaintenanceError(val estimatedOutage: String? = null) : SignInState()
    object Unknown : SignInState()
}
