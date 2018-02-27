package com.simprints.id.activities.login

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.secure.ProjectAuthenticator

interface LoginContract {

    interface View : BaseView<Presenter> {
        fun openScanQRApp()
        fun updateProjectSecretInTextView(projectSecret: String)
        fun updateProjectIdInTextView(projectId: String)
        fun showToast(stringRes: Int)
        fun showProgressDialog()
        fun dismissProgressDialog()
        fun returnSuccessfulResult()
    }

    interface Presenter : BasePresenter {
        var projectAuthenticator: ProjectAuthenticator

        fun userDidWantToOpenScanQRApp()
        fun userDidWantToSignIn(possibleProjectId: String,
                                possibleProjectSecret: String,
                                possibleUserId: String,
                                possibleLegacyApiKey: String? = null)

        fun processQRScannerAppResponse(scannedText: String)
    }
}
