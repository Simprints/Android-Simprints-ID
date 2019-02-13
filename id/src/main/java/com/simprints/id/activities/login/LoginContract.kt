package com.simprints.id.activities.login

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.secure.LegacyCompatibleProjectAuthenticator

interface LoginContract {

    interface View : BaseView<Presenter> {

        // QR code scanning

        fun handleOpenScanQRApp()
        fun handleMissingCredentials()
        fun showErrorForInvalidQRCode()
        fun updateProjectSecretInTextView(projectSecret: String)
        fun updateProjectIdInTextView(projectId: String)

        // Callbacks from signing in

        fun handleSignInFailedNoConnection()
        fun handleSignInFailedServerError()
        fun handleSignInFailedInvalidCredentials()
        fun handleSignInFailedProjectIdIntentMismatch()
        fun handleSignInFailedUnknownReason()
        fun handleSignInSuccess()
    }

    interface Presenter : BasePresenter {
        var projectAuthenticator: LegacyCompatibleProjectAuthenticator

        fun signIn(suppliedUserId: String,
                   suppliedProjectId: String,
                   suppliedProjectSecret: String,
                   intentProjectId: String? = null,
                   intentLegacyProjectId: String? = null)

        fun openScanQRApp()
        fun processQRScannerAppResponse(scannedText: String)
        fun logMessageForCrashReportWithUITrigger(message: String)
    }
}
