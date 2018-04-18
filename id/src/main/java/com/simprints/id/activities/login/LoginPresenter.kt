package com.simprints.id.activities.login

import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.exceptions.safe.secure.*
import com.simprints.id.secure.LegacyCompatibleProjectAuthenticator
import com.simprints.id.secure.models.NonceScope
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.IOException

class LoginPresenter(val view: LoginContract.View,
                     private val secureDataManager: SecureDataManager,
                     private val analyticsManager: AnalyticsManager,
                     override var projectAuthenticator: LegacyCompatibleProjectAuthenticator) : LoginContract.Presenter {

    companion object {
        private const val SCANNED_TEXT_TAG_PROJECT_ID = "project_id:"
        private const val SCANNED_TEXT_TAG_PROJECT_SECRET = "project_secret:"
    }

    override fun start() {}

    override fun signIn(suppliedUserId: String,
                        suppliedProjectId: String,
                        suppliedProjectSecret: String,
                        intentProjectId: String?,
                        intentLegacyProjectId: String?) =
        if (areMandatoryCredentialsPresent(suppliedProjectId, suppliedProjectSecret, suppliedUserId))
            doAuthenticate(
                suppliedProjectId,
                suppliedUserId,
                suppliedProjectSecret,
                intentProjectId,
                intentLegacyProjectId)
        else view.handleMissingCredentials()

    private fun areMandatoryCredentialsPresent(possibleProjectId: String, possibleProjectSecret: String, possibleUserId: String) =
        possibleProjectId.isNotEmpty() && possibleProjectSecret.isNotEmpty() && possibleUserId.isNotEmpty()

    private fun doAuthenticate(suppliedProjectId: String, suppliedUserId: String, suppliedProjectSecret: String, intentProjectId: String?, intentLegacyProjectId: String?) {
        secureDataManager.cleanCredentials()
        projectAuthenticator.authenticate(
            NonceScope(suppliedProjectId, suppliedUserId),
            suppliedProjectSecret,
            intentProjectId,
            intentLegacyProjectId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = { handleSignInSuccess() },
                onError = { e -> handleSignInError(e) })
    }

    private fun handleSignInSuccess() {
        view.handleSignInSuccess()
    }

    private fun handleSignInError(e: Throwable) {
        logSignInError(e)
        when (e) {
            is IOException -> view.handleSignInFailedNoConnection()
            is DifferentProjectIdReceivedFromIntentException -> view.handleSignInFailedProjectIdIntentMismatch()
            is InvalidLegacyProjectIdReceivedFromIntentException -> view.handleSignInFailedProjectIdIntentMismatch()
            is AuthRequestInvalidCredentialsException -> view.handleSignInFailedInvalidCredentials()
            is SimprintsInternalServerException -> view.handleSignInFailedServerError()
            else -> view.handleSignInFailedUnknownReason()
        }
    }

    private fun logSignInError(e: Throwable) {
        when (e) {
            is IOException -> Timber.d("Attempted login offline")
            else -> analyticsManager.logThrowable(e)
        }
    }

    override fun openScanQRApp() {
        view.handleOpenScanQRApp()
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
            view.showErrorForInvalidQRCode()
        }
    }
}
