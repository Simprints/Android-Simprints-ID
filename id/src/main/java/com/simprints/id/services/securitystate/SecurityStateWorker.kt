package com.simprints.id.services.securitystate

import android.content.Context
import androidx.work.WorkerParameters
import com.simprints.core.analytics.CrashReportManager
import com.simprints.id.Application
import com.simprints.id.secure.securitystate.SecurityStateProcessor
import com.simprints.id.secure.securitystate.repository.SecurityStateRepository
import com.simprints.id.services.sync.events.common.SimCoroutineWorker
import com.simprints.logging.Simber
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
    @Inject lateinit var securityStateProcessor: SecurityStateProcessor

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        (applicationContext as Application).component.inject(this@SecurityStateWorker)
        crashlyticsLog("Fetching security state")

        try {
            val securityState = repository.getSecurityState()
            securityStateProcessor.processSecurityState(securityState)
            success()
        } catch (t: Throwable) {
            Simber.e(t)
            success()
        }
    }

}
