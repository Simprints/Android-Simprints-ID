package com.simprints.infra.sync.config.usecase

import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.sync.ScheduleCommand
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.extensions.await
import javax.inject.Inject

internal class ResetLocalRecordsIfConfigChangedUseCase @Inject constructor(
    private val eventSyncManager: EventSyncManager,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val syncOrchestrator: SyncOrchestrator,
) {
    suspend operator fun invoke(
        oldConfig: ProjectConfiguration,
        newConfig: ProjectConfiguration,
    ) {
        if (hasPartitionTypeChanged(oldConfig, newConfig)) {
            syncOrchestrator.executeSchedulingCommand(
                ScheduleCommand.Events.rescheduleAfter {
                    eventSyncManager.resetDownSyncInfo()
                    enrolmentRecordRepository.deleteAll()
                },
            ).await()
        }
    }

    //
    private fun hasPartitionTypeChanged(
        oldConfig: ProjectConfiguration,
        newConfig: ProjectConfiguration,
    ) = (oldConfig.synchronization.down.commCare != newConfig.synchronization.down.commCare) ||
        // This also covers simprints changing from/to null since the partition will always be present if simprints is
        (
            oldConfig.synchronization.down.simprints
                ?.partitionType != newConfig.synchronization.down.simprints
                ?.partitionType
        )
}
