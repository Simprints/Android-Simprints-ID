package com.simprints.id.activities.login

import com.google.firebase.perf.FirebasePerformance
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdReceivedFromIntentException
import com.simprints.id.exceptions.safe.secure.InvalidLegacyProjectIdReceivedFromIntentException
import com.simprints.id.exceptions.safe.secure.SimprintsInternalServerException
import com.simprints.id.secure.LegacyCompatibleProjectAuthenticator
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.tools.Log
import com.simprints.id.tools.extensions.trace
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException


class LoginPresenter(val view: LoginContract.View,
                     private val secureDataManager: SecureDataManager,
                     private val analyticsManager: AnalyticsManager,
                     override var projectAuthenticator: LegacyCompatibleProjectAuthenticator) : LoginContract.Presenter {

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
            .trace("doAuthenticate")
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
     * {"projectId":"someProjectId","projectSecret":"someSecret"}
     **/
    override fun processQRScannerAppResponse(scannedText: String) {
        try {
            val scannedJson = JSONObject(scannedText)
            val potentialProjectId = scannedJson.getString(PROJECT_ID_JSON_KEY)
            val potentialProjectSecret = scannedJson.getString(PROJECT_SECRET_JSON_KEY)
            view.updateProjectIdInTextView(potentialProjectId)
            view.updateProjectSecretInTextView(potentialProjectSecret)
        } catch (e: JSONException) {
            view.showErrorForInvalidQRCode()
        }
    }

    companion object {
        private const val PROJECT_ID_JSON_KEY = "projectId"
        private const val PROJECT_SECRET_JSON_KEY = "projectSecret"
    }
}
