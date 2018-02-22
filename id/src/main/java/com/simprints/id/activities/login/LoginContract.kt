package com.simprints.id.activities.login

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.secure.ProjectAuthenticator
import com.simprints.id.secure.models.Tokens

interface LoginContract {

    interface View : BaseView<Presenter> {
        fun openScanQRApp()
        fun updateProjectSecretInTextView(projectSecret: String)
        fun updateProjectIdInTextView(projectId: String)
        fun showToast(stringRes: Int)
        fun showProgressDialog()
        fun dismissProgressDialog()
        fun returnSuccessfulResult(tokens: Tokens)
    }

    interface Presenter : BasePresenter {
        var projectAuthenticator: ProjectAuthenticator

        fun userDidWantToOpenScanQRApp()
        fun userDidWantToSignIn(possibleProjectId: String, possibleProjectSecret: String, possibleUserId: String, possibleLegacyApiKey: String? = null)
        fun processQRScannerAppResponse(scannedText: String)
    }
}
