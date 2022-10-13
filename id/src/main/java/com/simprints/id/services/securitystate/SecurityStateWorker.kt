package com.simprints.id.services.securitystate

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.id.secure.securitystate.SecurityStateProcessor
import com.simprints.id.secure.securitystate.repository.SecurityStateRepository
import com.simprints.id.services.sync.events.common.SimCoroutineWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.withContext

@HiltWorker
class SecurityStateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: SecurityStateRepository,
    private val securityStateProcessor: SecurityStateProcessor,
    private val dispatcher: DispatcherProvider,
) : SimCoroutineWorker(context, workerParams) {

    override val tag: String = SecurityStateWorker::class.java.simpleName

    override suspend fun doWork(): Result = withContext(dispatcher.io()) {
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
