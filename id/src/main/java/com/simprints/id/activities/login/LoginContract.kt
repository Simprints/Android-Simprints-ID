package com.simprints.id.activities.login

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.exceptions.safe.activities.InvalidScannedQRCodeText
import com.simprints.id.secure.LegacyCompatibleProjectAuthenticator

interface LoginContract {

    interface View : BaseView<Presenter> {

        // QR code scanning

        fun handleMissingCredentials()
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

        fun signIn(possibleUserId: String,
                   possibleProjectId: String,
                   possibleProjectSecret: String,
                   possibleLegacyProjectId: String? = null)

        @Throws(InvalidScannedQRCodeText::class)
        fun processQRScannerAppResponse(scannedText: String)
    }
}
