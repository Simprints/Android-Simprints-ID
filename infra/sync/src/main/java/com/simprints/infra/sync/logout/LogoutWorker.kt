package com.simprints.infra.sync.logout

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.simprints.core.DispatcherIO
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.sync.SyncConstants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Logout worker ensures that logout cleanup process is completed even if the app is closed during logout.
 *
 * While it may seem that the worker is misplaced, it is mostly doing the work similar to other sync workers:
 * - Unschedules all background work and clears sync caches
 * - Clears auth data and tokens
 * - Clears all local enrolment data
 */
@HiltWorker
class LogoutWorker @AssistedInject internal constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val logoutUseCase: LogoutUseCase,
    @param:DispatcherIO private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(context, params) {
    override val tag: String = "LogoutWorker"

    override suspend fun doWork(): Result = withContext(dispatcher) {
        crashlyticsLog("Started")

        val isProjectEnded =
            inputData.getBoolean(SyncConstants.LOGOUT_INPUT_IS_PROJECT_ENDED, false)
        try {
            logoutUseCase(isProjectEnded)
            success()
        } catch (t: Throwable) {
            fail(t)
        }
    }
}
