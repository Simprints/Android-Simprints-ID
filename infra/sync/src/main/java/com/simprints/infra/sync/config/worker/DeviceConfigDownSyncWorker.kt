package com.simprints.infra.sync.config.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.simprints.core.DispatcherBG
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.eventsync.module.ModuleSelectionRepository
import com.simprints.infra.sync.OneTime
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
    private val configRepository: ConfigRepository,
    private val logoutUseCase: LogoutUseCase,
    private val syncOrchestrator: SyncOrchestrator,
    private val moduleRepository: ModuleSelectionRepository,
    @param:DispatcherBG private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(context, params) {
    override val tag: String = "DeviceConfigDownSync"

    override suspend fun doWork(): Result = withContext(dispatcher) {
        showProgressNotification()
        crashlyticsLog("Started")
        try {
            val state = configRepository.getDeviceState()

            when {
                state.isCompromised -> logoutUseCase()
                // Device "commands" below are mutually exclusive in the backend response
                state.recordsToUpSync != null -> state.recordsToUpSync?.let { records ->
                    syncOrchestrator.uploadEnrolmentRecords(records.id, records.subjectIds)
                }
                state.selectModules != null -> state.selectModules?.let { modules ->
                    moduleRepository.forceModuleSelection(modules.moduleIds, isLocalChange = false)
                    configRepository.updateDeviceConfiguration { it.apply { lastInstructionId = modules.id } }
                    syncOrchestrator.execute(OneTime.Events.restart())
                }
            }
            success()
        } catch (t: Throwable) {
            fail(t)
        }
    }
}
