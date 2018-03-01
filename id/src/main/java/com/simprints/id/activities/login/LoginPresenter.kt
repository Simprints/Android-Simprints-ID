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
            secureDataManager.cleanCredentials()
            projectAuthenticator.authenticate(
                NonceScope(possibleProjectId, possibleUserId),
                possibleProjectSecret)
                .subscribe(
                    {
                        if (possibleLegacyApiKey != null) {
                            secureDataManager.storeProjectIdWithLegacyApiKeyPair(possibleProjectId, possibleLegacyApiKey)
                        }
                        secureDataManager.signedInProjectId = possibleProjectId
                        secureDataManager.signedInUserId = possibleUserId
                        view.dismissProgressDialog()
                        view.returnSuccessfulResult()
                    },
                    { e ->
                        e.printStackTrace()
                        view.dismissProgressDialog()
                        view.showToast(R.string.login_invalidCredentials)
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

        val projectIdRegex = "(?<=$SCANNED_TEXT_TAG_PROJECT_ID)(.*)"
        val projectSecretRegex = "(?<=$SCANNED_TEXT_TAG_PROJECT_SECRET)(.*)"

        val potentialProjectId = Regex(pattern = projectIdRegex).find(scannedText)?.value
        val potentialProjectSecret = Regex(pattern = projectSecretRegex).find(scannedText)?.value

        if (potentialProjectId != null && potentialProjectId.isNotEmpty() &&
            potentialProjectSecret != null && potentialProjectSecret.isNotEmpty() ) {

            view.updateProjectIdInTextView(potentialProjectId)
            view.updateProjectSecretInTextView(potentialProjectSecret)
        } else {
            throw Exception("Invalid scanned text")
        }
    }
}
