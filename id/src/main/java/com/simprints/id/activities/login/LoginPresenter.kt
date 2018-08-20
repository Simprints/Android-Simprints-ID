package com.simprints.id.activities.login

import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.eventData.SessionEventsManager
import com.simprints.id.data.analytics.eventData.models.events.AuthenticationEvent
import com.simprints.id.data.analytics.eventData.models.events.AuthenticationEvent.LoginInfo
import com.simprints.id.data.analytics.eventData.models.events.AuthenticationEvent.Result.*
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
                     private val component: AppComponent,
                     override var projectAuthenticator: LegacyCompatibleProjectAuthenticator) : LoginContract.Presenter {

    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var analyticsManager: AnalyticsManager
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
                    handleSignInSuccess()
                },
                onError = {
                    e ->
                    handleSignInError(e)
                })
    }

    private fun handleSignInSuccess() {
        addAuthenticatedEvent(AUTHENTICATED)
        view.handleSignInSuccess()
    }

    private fun addAuthenticatedEvent(result: AuthenticationEvent.Result) {
        sessionEventsManager.updateSessionInBackground({
            it.events.add(AuthenticationEvent(
                it.timeRelativeToStartTime(startTimeLogin),
                it.timeRelativeToStartTime(timeHelper.now()),
                LoginInfo(preferencesManager.projectId, preferencesManager.userId),
                result))
        })
    }

    private fun handleSignInError(e: Throwable) {
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

        addAuthenticatedEvent(reason)
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
