package com.simprints.id.services.securitystate

import android.content.Context
import androidx.work.WorkerParameters
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.id.Application
import com.simprints.id.secure.securitystate.SecurityStateProcessor
import com.simprints.id.secure.securitystate.repository.SecurityStateRepository
import com.simprints.id.services.sync.events.common.SimCoroutineWorker
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SecurityStateWorker(
    context: Context,
    workerParams: WorkerParameters,
) : SimCoroutineWorker(context, workerParams) {

    override val tag: String = SecurityStateWorker::class.java.simpleName

    @Inject lateinit var repository: SecurityStateRepository
    @Inject lateinit var securityStateProcessor: SecurityStateProcessor
    @Inject lateinit var dispatcher: DispatcherProvider

    override suspend fun doWork(): Result {
        (applicationContext as Application).component.inject(this@SecurityStateWorker)

        return withContext(dispatcher.io()) {
            crashlyticsLog("Fetching security state")

            try {
                val securityState = repository.getSecurityStatusFromRemote()
                securityStateProcessor.processSecurityState(securityState)
                success()
            } catch (t: Throwable) {
                fail(t)
            }
        }
    }

}
