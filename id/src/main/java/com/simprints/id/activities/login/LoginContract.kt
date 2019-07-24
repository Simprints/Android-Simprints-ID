package com.simprints.id.activities.login

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.secure.ProjectAuthenticator

interface LoginContract {

    interface View : BaseView<Presenter> {

        // QR code scanning

        fun handleOpenScanQRApp()
        fun handleMissingCredentials()
        fun showErrorForInvalidQRCode()
        fun updateProjectSecretInTextView(projectSecret: String)
        fun updateProjectIdInTextView(projectId: String)
        fun setErrorResponseInActivityResultAndFinish(appErrorResponse: AppErrorResponse)

        // Callbacks from signing in

        fun handleSignInFailedNoConnection()
        fun handleSignInFailedServerError()
        fun handleSignInFailedInvalidCredentials()
        fun handleSignInFailedProjectIdIntentMismatch()
        fun handleSignInFailedUnknownReason()
        fun handleSignInSuccess()
        fun handleSafetyNetDownError()
    }

    interface Presenter : BasePresenter {
        var projectAuthenticator: ProjectAuthenticator

        fun signIn(suppliedUserId: String,
                   suppliedProjectId: String,
                   suppliedProjectSecret: String,
                   intentProjectId: String? = null)

        fun openScanQRApp()
        fun processQRScannerAppResponse(scannedText: String)
        fun logMessageForCrashReportWithUITrigger(message: String)
        fun handleBackPressed()
    }
}
