package com.simprints.id.activities.login

import com.simprints.id.R
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdReceivedFromIntentException
import com.simprints.id.exceptions.safe.secure.InvalidLegacyProjectIdReceivedFromIntentException
import com.simprints.id.exceptions.safe.secure.SimprintsInternalServerException
import com.simprints.id.secure.LegacyCompatibleProjectAuthenticator
import com.simprints.id.secure.models.NonceScope
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber

@Suppress("UnnecessaryVariable")
class LoginPresenter(val view: LoginContract.View,
                     private val secureDataManager: SecureDataManager,
                     override var projectAuthenticator: LegacyCompatibleProjectAuthenticator) : LoginContract.Presenter {

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

        if (possibleProjectId.isNotEmpty() && possibleProjectSecret.isNotEmpty() && possibleUserId.isNotEmpty()) {
            view.showProgressDialog()
            doAuthenticate(possibleProjectId, possibleUserId, possibleProjectSecret, possibleLegacyApiKey)
        } else {
            view.showToast(R.string.login_missing_credentials)
        }
    }

    private fun doAuthenticate(possibleProjectId: String, possibleUserId: String, possibleProjectSecret: String, possibleLegacyApiKey: String?) {
        secureDataManager.cleanCredentials()
        projectAuthenticator.authenticate(
            NonceScope(possibleProjectId, possibleUserId),
            possibleProjectSecret,
            possibleLegacyApiKey)
            .subscribeBy(
                onSuccess = { handleSignInSuccess(possibleLegacyApiKey, possibleProjectId, possibleUserId) },
                onError = { e -> handleSignInError(e) })
    }

    private fun handleSignInSuccess(possibleLegacyApiKey: String?, possibleProjectId: String, possibleUserId: String) {
        secureDataManager.storeProjectIdWithLegacyApiKeyPair(possibleProjectId, possibleLegacyApiKey)
        secureDataManager.signedInProjectId = possibleProjectId
        secureDataManager.signedInUserId = possibleUserId
        view.dismissProgressDialog()
        view.returnSuccessfulResult()
    }

    private fun handleSignInError(e: Throwable) {
        when (e) {
            is DifferentProjectIdReceivedFromIntentException -> Timber.d(e)
            is InvalidLegacyProjectIdReceivedFromIntentException -> Timber.d(e)
            is AuthRequestInvalidCredentialsException -> Timber.d(e)
            is SimprintsInternalServerException -> Timber.d(e)
            else -> throw e
        }
        view.dismissProgressDialog()
        view.showToast(R.string.login_invalidCredentials)
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
            potentialProjectSecret != null && potentialProjectSecret.isNotEmpty()) {

            view.updateProjectIdInTextView(potentialProjectId)
            view.updateProjectSecretInTextView(potentialProjectSecret)
        } else {
            throw Exception("Invalid scanned text")
        }
    }
}
