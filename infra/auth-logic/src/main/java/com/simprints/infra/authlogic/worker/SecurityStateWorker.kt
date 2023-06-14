package com.simprints.infra.authlogic.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.simprints.core.DispatcherBG
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.projectsecuritystore.SecurityStateRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@HiltWorker
internal class SecurityStateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: SecurityStateRepository,
    private val securityStateProcessor: SecurityStateProcessor,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(context, workerParams) {

    override val tag: String = SecurityStateWorker::class.java.simpleName

    override suspend fun doWork(): Result =
        withContext(dispatcher) {
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
