package com.simprints.id.activities.login

import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.id.R
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.secure.ProjectAuthenticator
import com.simprints.id.secure.models.NonceScope

@Suppress("UnnecessaryVariable")
class LoginPresenter(val view: LoginContract.View,
                     private val secureDataManager: SecureDataManager,
                     private val dbManager: DbManager,
                     override var projectAuthenticator: ProjectAuthenticator,
                     private val safetyNetClient: SafetyNetClient) : LoginContract.Presenter {
    init {
        view.setPresenter(this)
    }

    override fun start() {
    }

    override fun userDidWantToOpenScanQRApp() {
        view.openScanQRApp()
    }

    override fun userDidWantToSignIn(possibleProjectId: String, possibleProjectSecret: String, possibleUserId: String) {

        if (!possibleProjectId.isEmpty() && !possibleProjectSecret.isEmpty() && !possibleUserId.isEmpty()) {
            view.showProgressDialog(R.string.progress_title, R.string.login_progress_message)
            projectAuthenticator.authenticateWithNewCredentials(
                safetyNetClient,
                NonceScope(possibleProjectId, possibleUserId),
                possibleProjectSecret)
                .subscribe(
                    { token ->
                        dbManager.signIn(token)
                        secureDataManager.signedInProjectId = possibleProjectId
                        view.returnSuccessfulResult()
                    },
                    { e ->
                        e.printStackTrace()
                        view.dismissProgressDialog()
                        view.showToast(R.string.login_invalidCredentials)
                        secureDataManager.cleanCredentials()
                    })
        } else {
            view.showToast(R.string.login_missing_credentials)
        }
    }

    override fun processQRScannerAppResponse(scannedText: String) {
        val potentialProjectId = scannedText.substring(scannedText.indexOf("id:") + "id:".length, scannedText.indexOf("\n"))
        val nextParam = scannedText.substring(scannedText.indexOf("\n") + 1)
        val potentialProjectSecret = nextParam.substring(nextParam.indexOf("secret:") + "secret:".length)

        view.updateProjectIdInTextView(potentialProjectId)
        view.updateProjectSecretInTextView(potentialProjectSecret)
    }
}
