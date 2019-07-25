package com.simprints.id.services

import android.annotation.SuppressLint
import android.app.IntentService
import android.content.Intent
import com.simprints.id.Application
import com.simprints.id.data.analytics.crashreport.CrashReportManager
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
            guidSelectionManager.handleIdentityConfirmationRequest(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onComplete = {
                    Timber.d("Added Guid Selection Event")
                })
        }
    }
}
