package com.simprints.id.activities.login

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView

interface LoginContract {

    interface View : BaseView<Presenter> {
        // QR code scanning

        fun handleMissingCredentials()
        fun showErrorForInvalidQRCode()
        fun updateProjectSecretInTextView(projectSecret: String)
        fun updateProjectIdInTextView(projectId: String)
    }

    interface Presenter : BasePresenter {
        fun processQRScannerAppResponse(scannedText: String)
        fun logMessageForCrashReportWithUITrigger(message: String)
    }
}
