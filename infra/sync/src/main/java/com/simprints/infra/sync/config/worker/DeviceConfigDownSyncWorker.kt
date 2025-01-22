package com.simprints.infra.sync.config.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.simprints.core.DispatcherBG
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.config.usecase.LogoutUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@HiltWorker
internal class DeviceConfigDownSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val configManager: ConfigManager,
    private val logoutUseCase: LogoutUseCase,
    private val syncOrchestrator: SyncOrchestrator,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(context, params) {
    override val tag: String = "DeviceConfigDownSync"

    override suspend fun doWork(): Result = withContext(dispatcher) {
        crashlyticsLog("Started")
        showProgressNotification()
        try {
            val state = configManager.getDeviceState()

            if (state.isCompromised) {
                logoutUseCase()
            } else if (state.recordsToUpSync != null) {
                state.recordsToUpSync?.let { records ->
                    syncOrchestrator.uploadEnrolmentRecords(records.id, records.subjectIds)
                }
            }
            success()
        } catch (t: Throwable) {
            fail(t)
        }
    }
}
