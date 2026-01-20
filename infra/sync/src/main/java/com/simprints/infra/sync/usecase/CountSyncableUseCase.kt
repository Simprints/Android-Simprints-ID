package com.simprints.infra.sync.usecase

import com.simprints.core.AppScope
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import com.simprints.infra.eventsync.sync.down.EventDownSyncPeriodicCountUseCase
import com.simprints.infra.sync.SyncableCounts
import com.simprints.infra.sync.usecase.internal.CountEnrolmentRecordsUseCase
import com.simprints.infra.sync.usecase.internal.CountSamplesToUploadUseCase
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
class CountSyncableUseCase @Inject internal constructor(
    private val countEnrolmentRecords: CountEnrolmentRecordsUseCase,
    private val countSamplesToUpload: CountSamplesToUploadUseCase,
    private val eventDownSyncCount: EventDownSyncPeriodicCountUseCase,
    private val eventRepository: EventRepository,
    @param:AppScope private val appScope: CoroutineScope,
) {
    private val sharedSyncableCounts: SharedFlow<SyncableCounts> by lazy {
        combine(
            totalRecordsCountFlow(),
            recordEventsToDownloadCountFlow(),
            combine( // nested combine, to stay within flow library standard combine limit of 5
                eventsToUploadCountFlow(),
                enrolmentsToUploadCountFlow(EventType.ENROLMENT_V2),
                enrolmentsToUploadCountFlow(EventType.ENROLMENT_V4),
            ) { eventsToUpload, enrolmentsToUploadV2, enrolmentsToUploadV4 ->
                Triple(eventsToUpload, enrolmentsToUploadV2, enrolmentsToUploadV4)
            },
            samplesToUploadCountFlow(),
        ) { totalRecords, recordEventsToDownload, (eventsToUpload, enrolmentsToUploadV2, enrolmentsToUploadV4), samplesToUpload ->
            val (recordEventsToDownloadCount, isRecordEventsToDownloadLowerBound) = recordEventsToDownload
            SyncableCounts(
                totalRecords,
                recordEventsToDownload = recordEventsToDownloadCount,
                isRecordEventsToDownloadLowerBound,
                eventsToUpload,
                enrolmentsToUploadV2,
                enrolmentsToUploadV4,
                samplesToUpload,
            )
        }.shareIn(
            appScope,
            SharingStarted.WhileSubscribed(),
            replay = 1,
        )
    }

    operator fun invoke(): Flow<SyncableCounts> = sharedSyncableCounts

    private fun totalRecordsCountFlow(): Flow<Int> = flow {
        emitAll(countEnrolmentRecords())
    }

    private fun recordEventsToDownloadCountFlow(): Flow<DownSyncCounts> = flow {
        emitAll(eventDownSyncCount())
    }

    private fun eventsToUploadCountFlow(): Flow<Int> = flow {
        emitAll(eventRepository.observeEventCount(type = null))
    }

    private fun enrolmentsToUploadCountFlow(type: EventType?): Flow<Int> = flow {
        emitAll(eventRepository.observeEventCount(type))
    }

    private fun samplesToUploadCountFlow(): Flow<Int> = flow {
        emitAll(countSamplesToUpload())
    }
}
