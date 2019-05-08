package com.simprints.id.services

import android.annotation.SuppressLint
import android.app.IntentService
import android.content.Intent
import com.google.gson.Gson
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.CalloutEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.ConfirmationCallout
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.app.requests.AppIdentityConfirmationRequest
import com.simprints.id.exceptions.safe.secure.NotSignedInException
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.deviceId
import com.simprints.id.tools.extensions.parseAppConfirmation
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class GuidSelectionService : IntentService("GuidSelectionService") {

    @Inject lateinit var guidSelectionManager: GuidSelectionManager
    @Inject lateinit var crashReportManager: CrashReportManager

    override fun onCreate() {
        super.onCreate()
        (application as Application).component.inject(this)
    }

    @SuppressLint("CheckResult")
    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val request = intent.parseAppConfirmation()
            guidSelectionManager.saveGUIDSelection(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onComplete = {
                    Timber.d("Added Guid Selection Event")
                }, onError = { e ->
                    crashReportManager.logExceptionOrThrowable(e)
                })
        }
    }
}
