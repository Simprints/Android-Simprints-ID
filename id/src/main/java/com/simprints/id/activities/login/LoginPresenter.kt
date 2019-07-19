package com.simprints.id.activities.login

import android.annotation.SuppressLint
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.AuthenticationEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.AuthenticationEvent.Result.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.AuthenticationEvent.UserInfo
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.di.AppComponent
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdReceivedFromIntentException
import com.simprints.id.exceptions.safe.secure.SafetyNetException
import com.simprints.id.exceptions.safe.secure.SafetyNetExceptionReason
import com.simprints.id.secure.ProjectAuthenticator
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
                     override var projectAuthenticator: ProjectAuthenticator) : LoginContract.Presenter {

    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var timeHelper: TimeHelper

    private var startTimeLogin: Long = 0

    init {
        component.inject(this)
    }

    override fun start() {}

    override fun signIn(suppliedUserId: String,
                        suppliedProjectId: String,
                        suppliedProjectSecret: String,
                        intentProjectId: String?) =
        if (areMandatoryCredentialsPresent(suppliedProjectId, suppliedProjectSecret, suppliedUserId)) {

            doAuthenticate(
                suppliedProjectId,
                suppliedUserId,
                suppliedProjectSecret)
        }
        else {
            view.handleMissingCredentials()
        }

    private fun areMandatoryCredentialsPresent(possibleProjectId: String, possibleProjectSecret: String, possibleUserId: String) =
        possibleProjectId.isNotEmpty() && possibleProjectSecret.isNotEmpty() && possibleUserId.isNotEmpty()

    @SuppressLint("CheckResult")
    private fun doAuthenticate(suppliedProjectId: String,
                               suppliedUserId: String,
                               suppliedProjectSecret: String) {

        logMessageForCrashReportWithNetworkTrigger("Making authentication request")
        loginInfoManager.cleanCredentials()
        startTimeLogin = timeHelper.now()
        projectAuthenticator.authenticate(
            NonceScope(suppliedProjectId, suppliedUserId),
            suppliedProjectSecret)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .trace("doAuthenticate")
            .subscribeBy(
                onComplete = {
                    handleSignInSuccess(suppliedProjectId, suppliedUserId)
                },
                onError = { e ->
                    e.printStackTrace()
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

            it.addEvent(AuthenticationEvent(
                startTimeLogin,
                timeHelper.now(),
                UserInfo(suppliedProjectId, suppliedUserId),
                result))
        }
    }

    private fun handleSignInError(e: Throwable,
                                  suppliedProjectId: String,
                                  suppliedUserId: String) {
        logSignInError(e)
        var reason: AuthenticationEvent.Result
        when (e) {
            is IOException -> view.handleSignInFailedNoConnection().also { reason = OFFLINE }
            is DifferentProjectIdReceivedFromIntentException -> view.handleSignInFailedProjectIdIntentMismatch().also { reason = BAD_CREDENTIALS }
            is AuthRequestInvalidCredentialsException -> view.handleSignInFailedInvalidCredentials().also { reason = BAD_CREDENTIALS }
            is SimprintsInternalServerException -> view.handleSignInFailedServerError().also { reason = TECHNICAL_FAILURE }
            is SafetyNetException -> view.handleSafetyNetDownError().also {
                reason = getSafetyNetErrorForAuthenticationEvent(e.reason)
            }
            else -> view.handleSignInFailedUnknownReason().also { reason = TECHNICAL_FAILURE }
        }

        logMessageForCrashReportWithNetworkTrigger("Sign in reason - $reason")
        addAuthenticatedEventAndUpdateProjectIdIfRequired(reason, suppliedProjectId, suppliedUserId)
    }

    private fun getSafetyNetErrorForAuthenticationEvent(e: SafetyNetExceptionReason) =
        when (e) {
            SafetyNetExceptionReason.SERVICE_UNAVAILABLE -> SAFETYNET_UNAVAILABLE
            SafetyNetExceptionReason.INVALID_CLAIMS -> SAFETYNET_INVALID_CLAIM
        }

    private fun logSignInError(e: Throwable) {
        when (e) {
            is IOException -> Timber.d("Attempted login offline")
            else -> crashReportManager.logExceptionOrSafeException(e)
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
        crashReportManager.logMessageForCrashReport(CrashReportTag.LOGIN, CrashReportTrigger.UI, message = message)
    }

    private fun logMessageForCrashReportWithNetworkTrigger(message: String) {
        crashReportManager.logMessageForCrashReport(CrashReportTag.LOGIN, CrashReportTrigger.NETWORK, message = message)
    }

    companion object {
        private const val PROJECT_ID_JSON_KEY = "projectId"
        private const val PROJECT_SECRET_JSON_KEY = "projectSecret"
    }
}
