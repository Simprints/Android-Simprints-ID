package com.simprints.id.activities.login

import com.simprints.id.data.analytics.crashReport.CrashReportManager
import com.simprints.id.data.analytics.crashReport.CrashReportTags
import com.simprints.id.data.analytics.crashReport.CrashTrigger
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventData.models.domain.events.AuthenticationEvent
import com.simprints.id.data.analytics.eventData.models.domain.events.AuthenticationEvent.Result.*
import com.simprints.id.data.analytics.eventData.models.domain.events.AuthenticationEvent.UserInfo
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdReceivedFromIntentException
import com.simprints.id.exceptions.safe.secure.InvalidLegacyProjectIdReceivedFromIntentException
import com.simprints.id.secure.LegacyCompatibleProjectAuthenticator
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.trace
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class LoginPresenter(val view: LoginContract.View,
                     component: AppComponent,
                     override var projectAuthenticator: LegacyCompatibleProjectAuthenticator) : LoginContract.Presenter {

    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var timeHelper: TimeHelper

    private var startTimeLogin: Long = 0

    init {
        component.inject(this)
    }

    override fun start() {}

    override fun signIn(suppliedUserId: String,
                        suppliedProjectId: String,
                        suppliedProjectSecret: String,
                        intentProjectId: String?,
                        intentLegacyProjectId: String?) =
        if (areMandatoryCredentialsPresent(suppliedProjectId, suppliedProjectSecret, suppliedUserId)) {
            doAuthenticate(
                suppliedProjectId,
                suppliedUserId,
                suppliedProjectSecret,
                intentProjectId,
                intentLegacyProjectId)
        }
        else {
            view.handleMissingCredentials()
        }

    private fun areMandatoryCredentialsPresent(possibleProjectId: String, possibleProjectSecret: String, possibleUserId: String) =
        possibleProjectId.isNotEmpty() && possibleProjectSecret.isNotEmpty() && possibleUserId.isNotEmpty()

    private fun doAuthenticate(suppliedProjectId: String,
                               suppliedUserId: String,
                               suppliedProjectSecret: String, intentProjectId: String?, intentLegacyProjectId: String?) {

        logMessageForCrashReportWithNetworkTrigger("Making authentication request")
        loginInfoManager.cleanCredentials()
        startTimeLogin = timeHelper.now()
        projectAuthenticator.authenticate(
            NonceScope(suppliedProjectId, suppliedUserId),
            suppliedProjectSecret,
            intentProjectId,
            intentLegacyProjectId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .trace("doAuthenticate")
            .subscribeBy(
                onComplete = {
                    handleSignInSuccess(suppliedProjectId, suppliedUserId)
                },
                onError = { e ->
                    handleSignInError(e, suppliedProjectId, suppliedUserId)
                })
    }

    private fun handleSignInSuccess(suppliedProjectId: String,
                                    suppliedUserId: String) {
        logMessageForCrashReportWithNetworkTrigger("Sign in success")
        addAuthenticatedEventAndUpdateProjectIdIfRequired(AUTHENTICATED, suppliedProjectId, suppliedUserId)
        view.handleSignInSuccess()
    }

    private fun addAuthenticatedEventAndUpdateProjectIdIfRequired(result: AuthenticationEvent.Result,
                                                                  suppliedProjectId: String,
                                                                  suppliedUserId: String) {

        sessionEventsManager.updateSessionInBackground {
            if (result == AUTHENTICATED) {
                it.projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
            }

            it.events.add(AuthenticationEvent(
                it.timeRelativeToStartTime(startTimeLogin),
                it.timeRelativeToStartTime(timeHelper.now()),
                UserInfo(suppliedProjectId, suppliedUserId),
                result))
        }
    }

    private fun handleSignInError(e: Throwable,
                                  suppliedProjectId: String,
                                  suppliedUserId: String) {
        logSignInError(e)
        var reason = TECHNICAL_FAILURE
        when (e) {
            is IOException -> view.handleSignInFailedNoConnection().also { reason = OFFLINE }
            is DifferentProjectIdReceivedFromIntentException -> view.handleSignInFailedProjectIdIntentMismatch().also { reason = BAD_CREDENTIALS }
            is InvalidLegacyProjectIdReceivedFromIntentException -> view.handleSignInFailedProjectIdIntentMismatch().also { reason = BAD_CREDENTIALS }
            is AuthRequestInvalidCredentialsException -> view.handleSignInFailedInvalidCredentials().also { reason = BAD_CREDENTIALS }
            is SimprintsInternalServerException -> view.handleSignInFailedServerError().also { reason = TECHNICAL_FAILURE }
            else -> view.handleSignInFailedUnknownReason().also { reason = TECHNICAL_FAILURE }
        }

        logMessageForCrashReportWithNetworkTrigger("Sign in error - $reason")
        addAuthenticatedEventAndUpdateProjectIdIfRequired(reason, suppliedProjectId, suppliedUserId)
    }

    private fun logSignInError(e: Throwable) {
        when (e) {
            is IOException -> Timber.d("Attempted login offline")
            else -> crashReportManager.logExceptionOrThrowable(e)
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
            logMessageForCrashReportWithUITrigger("QR scanning successful")
        } catch (e: JSONException) {
            view.showErrorForInvalidQRCode()
            logMessageForCrashReportWithUITrigger("QR scanning unsuccessful")
        }
    }

    override fun logMessageForCrashReportWithUITrigger(message: String) {
        crashReportManager.logInfo(CrashReportTags.LOGIN, CrashTrigger.UI, message)
    }

    private fun logMessageForCrashReportWithNetworkTrigger(message: String) {
        crashReportManager.logInfo(CrashReportTags.LOGIN, CrashTrigger.NETWORK, message)
    }

    companion object {
        private const val PROJECT_ID_JSON_KEY = "projectId"
        private const val PROJECT_SECRET_JSON_KEY = "projectSecret"
    }
}
