package com.simprints.id.services

import android.app.IntentService
import android.content.Intent
import com.simprints.id.Application
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.controllers.remote.apiAdapters.SessionEventsApiAdapterFactory
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.requests.IdConfirmIdentifyRequest
import com.simprints.id.exceptions.safe.secure.NotSignedInException
import com.simprints.id.exceptions.safe.callout.InvalidCalloutParameterError
import com.simprints.id.secure.cryptography.Hasher
import com.simprints.id.tools.extensions.parseClientApiRequest
import com.simprints.libsimprints.Constants.SIMPRINTS_PROJECT_ID
import com.simprints.libsimprints.Constants.SIMPRINTS_SESSION_ID
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class GuidSelectionService : IntentService("GuidSelectionService") {

    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var sessionEventsManager: SessionEventsManager

    override fun onCreate() {
        super.onCreate()
        (application as Application).component.inject(this)
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            onHandleNonNullIntent(intent.parseClientApiRequest() as IdConfirmIdentifyRequest)
        } else {
            analyticsManager.logGuidSelectionService("", "", "", false)
        }
    }

    private fun onHandleNonNullIntent(intent: IdConfirmIdentifyRequest) {
        val projectId = intent.projectId
        val sessionId = intent.sessionId
        val selectedGuid = intent.selectedGuid
        val callbackSent = try {
            checkCalloutParameters(projectId, sessionId, selectedGuid)
            dbManager.updateIdentification(loginInfoManager.getSignedInProjectIdOrEmpty(), selectedGuid, sessionId ?: "")
            sessionId?.let {
                sessionEventsManager
                    .addGuidSelectionEventToLastIdentificationIfExists(selectedGuid, sessionId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(onComplete = {
                        Timber.d(SessionEventsApiAdapterFactory().gson.toJson(sessionEventsManager.loadSessionById(sessionId).blockingGet()))
                        Timber.d("Added Guid Selection Event")
                    }, onError = { e ->
                        crashReportManager.logExceptionOrThrowable(e)
                    })
            }
            true
        } catch (t: Throwable) {
            crashReportManager.logExceptionOrThrowable(t)
            false
        }
        analyticsManager.logGuidSelectionService(loginInfoManager.getSignedInProjectIdOrEmpty(),
            sessionId ?: "", selectedGuid, callbackSent)
    }

    private fun checkCalloutParameters(projectId: String?, sessionId: String?, selectedGuid: String) {
        checkProjectIdOrApiKey(projectId)
        checkSessionId(sessionId)
        checkSelectedGuid(selectedGuid)
    }

    private fun checkSessionId(sessionId: String?) {
        if (sessionId == null) {
            throw InvalidCalloutParameterError.forParameter(SIMPRINTS_SESSION_ID)
        }
    }

    private fun checkProjectIdOrApiKey(projectId: String?) =
        when {
            projectId != null -> checkProjectId(projectId)
            else -> throw InvalidCalloutParameterError.forParameter(SIMPRINTS_PROJECT_ID)
        }

    private fun checkProjectId(projectId: String) {
        if (!loginInfoManager.isProjectIdSignedIn(projectId)) throw NotSignedInException()
    }

    private fun checkApiKey(apiKey: String) {
        val potentialProjectId = loginInfoManager.getProjectIdForHashedLegacyProjectIdOrEmpty(Hasher().hash(apiKey))
        if (!loginInfoManager.isProjectIdSignedIn(potentialProjectId)) throw NotSignedInException()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun checkSelectedGuid(selectedGuid: String) {
        // For now, any selected guid is valid
    }
}
