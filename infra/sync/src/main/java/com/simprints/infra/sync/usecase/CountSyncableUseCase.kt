package com.simprints.infra.sync.usecase

import com.simprints.core.AppScope
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import com.simprints.infra.eventsync.sync.down.EventDownSyncPeriodicCountUseCase
import com.simprints.infra.sync.SyncableCounts
import com.simprints.infra.sync.usecase.internal.CountEnrolmentRecordsUseCase
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
 * Combines relevant syncable entity counts in the current project (for events & images)
 * together in a reactive way.
 */
@Singleton
class CountSyncableUseCase @Inject constructor(
    private val countEnrolmentRecords: CountEnrolmentRecordsUseCase,
    private val countImagesToUpload: CountImagesToUploadUseCase,
    private val eventDownSyncCount: EventDownSyncPeriodicCountUseCase,
    private val eventRepository: EventRepository,
    @param:AppScope private val appScope: CoroutineScope,
) {
    private val sharedSyncableCounts: SharedFlow<SyncableCounts> by lazy {
        combine(
            enrolmentRecordCountFlow(),
            downloadsEventCountFlow(),
            combine( // nested combine, to stay within flow library standard combine limit of 5
                uploadEventCountFlow(null),
                uploadEventCountFlow(EventType.ENROLMENT_V2),
                uploadEventCountFlow(EventType.ENROLMENT_V4),
            ) { upload, uploadEnrolmentV2, uploadEnrolmentV4 ->
                Triple(upload, uploadEnrolmentV2, uploadEnrolmentV4)
            },
            uploadImageCountFlow(),
        ) { total, downloadSyncCounts, (upload, uploadEnrolmentV2, uploadEnrolmentV4), uploadImages ->
            val (download, isDownloadLowerBound) = downloadSyncCounts
            SyncableCounts(
                total,
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
            replay = 1,
        )
    }

    operator fun invoke(): Flow<SyncableCounts> = sharedSyncableCounts

    private fun enrolmentRecordCountFlow(): Flow<Int> = flow {
        emitAll(countEnrolmentRecords())
    }

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
