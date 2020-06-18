package com.simprints.id.services.securitystate

import android.content.Context
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.secure.securitystate.repository.SecurityStateRepository
import com.simprints.id.services.scheduledSync.subjects.common.SimCoroutineWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SecurityStateWorker(
    context: Context,
    workerParams: WorkerParameters
) : SimCoroutineWorker(context, workerParams) {

    override val tag: String = SecurityStateWorker::class.java.simpleName

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var repository: SecurityStateRepository

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        (applicationContext as Application).component.inject(this@SecurityStateWorker)
        crashlyticsLog("Fetching security state")

        try {
            repository.getSecurityState()
            // TODO: handle result with security state processor (PAS-900)
            success()
        } catch (t: Throwable) {
            crashReportManager.logExceptionOrSafeException(t)
            retry()
        }
    }

}
