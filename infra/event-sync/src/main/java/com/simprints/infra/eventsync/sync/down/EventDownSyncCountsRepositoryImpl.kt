package com.simprints.infra.eventsync.sync.down

import com.simprints.core.domain.tokenization.values
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSource
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class EventDownSyncCountsRepositoryImpl @Inject constructor(
    private val configRepository: ConfigRepository,
    private val downSyncScopeRepository: EventDownSyncScopeRepository,
    private val eventRemoteDataSource: EventRemoteDataSource,
) : EventDownSyncCountsRepository {
    override suspend fun countEventsToDownload(): DownSyncCounts {
        val projectConfig = configRepository.getProjectConfiguration()
        val simprintsDownConfig = projectConfig.synchronization.down.simprints
        // For CommCare there's no easy way to count the number of events to download
        if (simprintsDownConfig == null) {
            return DownSyncCounts(count = 0, isLowerBound = false)
        }
        val deviceConfig = configRepository.getDeviceConfiguration()

        val downSyncScope = downSyncScopeRepository.getDownSyncScope(
            modes = projectConfig.general.modalities,
            selectedModuleIDs = deviceConfig.selectedModules.values(),
            syncPartitioning = simprintsDownConfig.partitionType.toDomain(),
        )

        val counts = downSyncScope.operations
            .map { eventRemoteDataSource.count(it.queryEvent.fromDomainToApi()) }

        return DownSyncCounts(
            count = counts.sumOf { it.count },
            isLowerBound = counts.any { it.isLowerBound },
        )
    }
}
