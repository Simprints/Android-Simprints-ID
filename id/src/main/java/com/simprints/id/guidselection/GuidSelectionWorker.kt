package com.simprints.id.guidselection

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.orchestrator.steps.core.requests.GuidSelectionRequest
import com.simprints.id.services.GuidSelectionManager
import com.simprints.id.services.scheduledSync.subjects.common.SimCoroutineWorker
import timber.log.Timber
import javax.inject.Inject

class GuidSelectionWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params) {

    @Inject lateinit var guidSelectionManager: GuidSelectionManager
    @Inject override lateinit var crashReportManager: CrashReportManager

    override val tag: String = GuidSelectionWorker::class.java.simpleName

    override suspend fun doWork(): Result {
        (applicationContext as Application).component.inject(this)

        handleGuidSelectionRequest()
        return Result.success()
    }

    @SuppressLint("CheckResult")
    private suspend fun handleGuidSelectionRequest() {
        try {
            val request = GuidSelectionRequest.fromMap(inputData.keyValueMap)
            guidSelectionManager.handleConfirmIdentityRequest(request)
            Timber.d("Added Guid Selection Event")
            crashReportManager.logMessageForCrashReport(CrashReportTag.SESSION,
                CrashReportTrigger.UI, message = "Added Guid Selection Event")
        } catch (t: Throwable) {
            Timber.e(t)
            crashReportManager.logException(t)
        }
    }

}
