package com.simprints.id.services

import android.app.IntentService
import android.content.Intent
import com.google.gson.Gson
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.requests.IdentityConfirmationRequest
import com.simprints.id.exceptions.safe.secure.NotSignedInException
import com.simprints.id.tools.extensions.deviceId
import com.simprints.id.tools.extensions.parseClientApiRequest
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
            onHandleNonNullIntent(intent.parseClientApiRequest() as IdentityConfirmationRequest)
        }
    }

    private fun onHandleNonNullIntent(intent: IdentityConfirmationRequest) {
        val projectId = intent.projectId
        val sessionId = intent.sessionId
        val selectedGuid = intent.selectedGuid
        val callbackSent = try {
            checkProjectId(projectId)
            sessionId.let {
                sessionEventsManager
                    .addGuidSelectionEventToLastIdentificationIfExists(selectedGuid, sessionId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(onComplete = {
                        if(BuildConfig.DEBUG) {
                            Timber.d(Gson().toJson(sessionEventsManager.loadSessionById(sessionId).blockingGet()))
                        }
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
        analyticsManager.logGuidSelectionService(
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            sessionId,
            baseContext.deviceId,
            selectedGuid,
            callbackSent)
    }

    private fun checkProjectId(projectId: String) {
        if (!loginInfoManager.isProjectIdSignedIn(projectId)) throw NotSignedInException()
    }
}
