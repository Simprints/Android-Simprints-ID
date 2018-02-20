package com.simprints.id.activities.login

import com.simprints.id.R
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.secure.ProjectAuthenticator
import com.simprints.id.secure.models.NonceScope

@Suppress("UnnecessaryVariable")
class LoginPresenter(val view: LoginContract.View,
                     private val secureDataManager: SecureDataManager,
                     override var projectAuthenticator: ProjectAuthenticator) : LoginContract.Presenter {

    companion object {
        private const val SCANNED_TEXT_TAG_PROJECT_ID = "id:"
        private const val SCANNED_TEXT_TAG_PROJECT_SECRET = "secret:"
    }
    init {
        view.setPresenter(this)
    }

    override fun start() {
    }

    override fun userDidWantToOpenScanQRApp() {
        view.openScanQRApp()
    }

    override fun userDidWantToSignIn(possibleProjectId: String, possibleProjectSecret: String, possibleUserId: String, possibleLegacyApiKey: String?) {

        if (!possibleProjectId.isEmpty() && !possibleProjectSecret.isEmpty() && !possibleUserId.isEmpty()) {
            view.showProgressDialog(R.string.progress_title, R.string.login_progress_message)
            projectAuthenticator.authenticate(
                NonceScope(possibleProjectId, possibleUserId),
                possibleProjectSecret)
                .subscribe(
                    { token ->
                        secureDataManager.storeProjectIdWithLegacyApiKeyPair(possibleProjectId, possibleLegacyApiKey)
                        secureDataManager.signedInProjectId = possibleProjectId
                        view.dismissProgressDialog()
                        view.returnSuccessfulResult(token)
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

    /** Valid Scanned Text Format:
     * id:someProjectId\n
     * secret:someSecret
     **/
    override fun processQRScannerAppResponse(scannedText: String) {

        val beginProjectId = scannedText.indexOf(SCANNED_TEXT_TAG_PROJECT_ID) + SCANNED_TEXT_TAG_PROJECT_ID.length
        val endProjectId = scannedText.indexOf("\n")
        val potentialProjectId = scannedText.substring(beginProjectId, endProjectId)

        val nextParamRow = scannedText.substring(scannedText.indexOf("\n") + 1)
        val beginProjectSecret = nextParamRow.indexOf(SCANNED_TEXT_TAG_PROJECT_SECRET) + SCANNED_TEXT_TAG_PROJECT_SECRET.length
        val potentialProjectSecret = nextParamRow.substring(beginProjectSecret)

        view.updateProjectIdInTextView(potentialProjectId)
        view.updateProjectSecretInTextView(potentialProjectSecret)
    }
}
