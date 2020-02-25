package com.simprints.id.guidselection

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.domain.moduleapi.app.requests.AppIdentityConfirmationRequest
import com.simprints.id.services.GuidSelectionManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class GuidSelectionWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    @Inject lateinit var guidSelectionManager: GuidSelectionManager

    @Inject lateinit var crashReportManager: CrashReportManager

    override fun doWork(): Result {
        (applicationContext as Application).component.inject(this)
        handleGuidSelectionRequest()
        return Result.success()
    }

    @SuppressLint("CheckResult")
    private fun handleGuidSelectionRequest() {
        val request = AppIdentityConfirmationRequest.fromMap(inputData.keyValueMap)
        guidSelectionManager.handleIdentityConfirmationRequest(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onComplete = {
                Timber.d("Added Guid Selection Event")
                crashReportManager.logMessageForCrashReport(CrashReportTag.SESSION,
                    CrashReportTrigger.UI, message = "Added Guid Selection Event")
            }, onError = {
                Timber.e(it)
                crashReportManager.logException(it)
            })
    }

}
