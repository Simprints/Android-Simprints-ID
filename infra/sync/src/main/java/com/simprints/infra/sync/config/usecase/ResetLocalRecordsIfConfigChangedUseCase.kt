package com.simprints.infra.sync.config.usecase

import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.eventsync.ResetDownSyncInfoUseCase
import com.simprints.infra.sync.ScheduleCommand
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.extensions.await
import javax.inject.Inject

internal class ResetLocalRecordsIfConfigChangedUseCase @Inject constructor(
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val resetDownSyncInfo: ResetDownSyncInfoUseCase,
    private val syncOrchestrator: SyncOrchestrator,
) {
    suspend operator fun invoke(
        oldConfig: ProjectConfiguration,
        newConfig: ProjectConfiguration,
    ) {
        if (hasPartitionTypeChanged(oldConfig, newConfig)) {
            syncOrchestrator
                .execute(
                    ScheduleCommand.Events.rescheduleAfter {
                        resetDownSyncInfo()
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
        oldConfig.synchronization.down.simprints
            ?.partitionType != newConfig.synchronization.down.simprints
            ?.partitionType
}
