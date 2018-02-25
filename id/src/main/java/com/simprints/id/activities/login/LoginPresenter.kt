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
        private const val SCANNED_TEXT_TAG_PROJECT_ID = "project_id:"
        private const val SCANNED_TEXT_TAG_PROJECT_SECRET = "project_secret:"
    }

    init {
        view.setPresenter(this)
    }

    override fun start() {
    }

    override fun userDidWantToOpenScanQRApp() {
        view.openScanQRApp()
    }

    override fun userDidWantToSignIn(possibleProjectId: String,
                                     possibleProjectSecret: String,
                                     possibleUserId: String,
                                     possibleLegacyApiKey: String?) {

        if (possibleProjectId.isNotEmpty() &&
            possibleProjectSecret.isNotEmpty() &&
            possibleUserId.isNotEmpty()) {

            view.showProgressDialog()
            projectAuthenticator.authenticate(
                NonceScope(possibleProjectId, possibleUserId),
                possibleProjectSecret)
                .subscribe(
                    {
                        secureDataManager.storeProjectIdWithLegacyApiKeyPair(possibleProjectId, possibleLegacyApiKey)
                        secureDataManager.signedInProjectId = possibleProjectId
                        secureDataManager.signedInUserId = possibleUserId
                        view.dismissProgressDialog()
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

    /**
     * Valid Scanned Text Format:
     * project_id:someProjectId\n
     * project_secret:someSecret
     **/
    override fun processQRScannerAppResponse(scannedText: String) {

        val potentialProjectId = Regex(pattern = "(?<=$SCANNED_TEXT_TAG_PROJECT_ID)(.*)").find(scannedText)?.value
        val potentialProjectSecret = Regex(pattern = "(?<=$SCANNED_TEXT_TAG_PROJECT_SECRET)(.*)").find(scannedText)?.value

        if (potentialProjectId != null && potentialProjectId.isNotEmpty() &&
            potentialProjectSecret != null && potentialProjectSecret.isNotEmpty() ) {

            view.updateProjectIdInTextView(potentialProjectId)
            view.updateProjectSecretInTextView(potentialProjectSecret)
        } else {
            throw Exception("Invalid scanned text")
        }
    }
}
