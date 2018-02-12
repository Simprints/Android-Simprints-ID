package com.simprints.id.activities.login

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.secure.ProjectAuthenticator
import com.simprints.id.secure.models.Token

interface LoginContract {

    interface View : BaseView<Presenter> {
        fun openScanQRApp()
        fun updateProjectSecretInTextView(projectSecret: String)
        fun updateProjectIdInTextView(projectId: String)
        fun showToast(StringRes: Int)
        fun showProgressDialog(title: Int, message: Int)
        fun dismissProgressDialog()
        fun returnSuccessfulResult(token: Token)
    }

    interface Presenter : BasePresenter {
        var projectAuthenticator:ProjectAuthenticator

        fun userDidWantToOpenScanQRApp()
        fun userDidWantToSignIn(possibleProjectId: String, possibleProjectSecret: String, possibleUserId: String)
        fun processQRScannerAppResponse(scannedText: String)
    }
}
