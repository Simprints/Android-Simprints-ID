package com.simprints.infra.sync.config.usecase

import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.sync.SyncOrchestrator
import javax.inject.Inject

internal class ResetLocalRecordsIfConfigChangedUseCase @Inject constructor(
    private val syncOrchestrator: SyncOrchestrator,
    private val eventSyncManager: EventSyncManager,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
) {
    suspend operator fun invoke(
        oldConfig: ProjectConfiguration,
        newConfig: ProjectConfiguration,
    ) {
        if (hasPartitionTypeChanged(oldConfig, newConfig)) {
            syncOrchestrator.cancelEventSync()
            eventSyncManager.resetDownSyncInfo()
            enrolmentRecordRepository.deleteAll()
            syncOrchestrator.rescheduleEventSync()
        }
    }

    private fun hasPartitionTypeChanged(
        oldConfig: ProjectConfiguration,
        newConfig: ProjectConfiguration,
    ) = oldConfig.synchronization.down.simprints.partitionType != newConfig.synchronization.down.simprints.partitionType
}
