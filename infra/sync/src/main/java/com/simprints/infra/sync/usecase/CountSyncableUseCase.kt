package com.simprints.infra.sync.usecase

import com.simprints.core.AppScope
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import com.simprints.infra.eventsync.sync.down.EventDownSyncPeriodicCountUseCase
import com.simprints.infra.sync.SyncableCounts
import com.simprints.infra.sync.usecase.internal.CountImagesToUploadUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Combines relevant syncable entity counts (for events & images) together in a reactive way.
 */
@Singleton
class CountSyncableUseCase @Inject constructor(
    private val countImagesToUpload: CountImagesToUploadUseCase,
    private val eventDownSyncCount: EventDownSyncPeriodicCountUseCase,
    private val eventRepository: EventRepository,
    @param:AppScope private val appScope: CoroutineScope,
) {
    private val sharedSyncableCounts: SharedFlow<SyncableCounts> by lazy {
        combine(
            downloadsEventCountFlow(),
            uploadEventCountFlow(null),
            uploadEventCountFlow(EventType.ENROLMENT_V2),
            uploadEventCountFlow(EventType.ENROLMENT_V4),
            uploadImageCountFlow(),
        ) { downloadSyncCounts, upload, uploadEnrolmentV2, uploadEnrolmentV4, uploadImages ->
            val (download, isDownloadLowerBound) = downloadSyncCounts
            SyncableCounts(
                download,
                isDownloadLowerBound,
                upload,
                uploadEnrolmentV2,
                uploadEnrolmentV4,
                uploadImages,
            )
        }.shareIn(
            appScope,
            SharingStarted.WhileSubscribed(),
        )
    }

    operator fun invoke(): Flow<SyncableCounts> = sharedSyncableCounts

    private fun downloadsEventCountFlow(): Flow<DownSyncCounts> = flow {
        emitAll(eventDownSyncCount())
    }

    private fun uploadEventCountFlow(type: EventType?): Flow<Int> = flow {
        emitAll(eventRepository.observeEventCount(type))
    }

    private fun uploadImageCountFlow(): Flow<Int> = flow {
        emitAll(countImagesToUpload())
    }

}
