package com.simprints.infra.sync.usecase

import com.simprints.core.AppScope
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.eventsync.sync.down.EventDownSyncPeriodicCountUseCase
import com.simprints.infra.sync.SyncableCounts
import com.simprints.infra.sync.usecase.internal.ObserveEnrolmentRecordsCountUseCase
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
    private val countEnrolmentRecords: ObserveEnrolmentRecordsCountUseCase,
    private val countSamplesToUpload: CountSamplesToUploadUseCase,
    private val eventDownSyncCount: EventDownSyncPeriodicCountUseCase,
    private val eventRepository: EventRepository,
    @param:AppScope private val appScope: CoroutineScope,
) {
    private val sharedSyncableCounts: SharedFlow<SyncableCounts> by lazy {
        combine(
            countEnrolmentRecords(),
            eventDownSyncCount(),
            flow { // recordEventsToDownload
                emitAll(eventRepository.observeEventCount(type = null))
            },
            flow { // eventsToUpload
                emitAll(
                    combine(
                        eventRepository.observeEventCount(EventType.ENROLMENT_V2),
                        eventRepository.observeEventCount(EventType.ENROLMENT_V4),
                    ) { countV2, countV4 ->
                        countV2 + countV4
                    }
                )
            },
            countSamplesToUpload(),
        ) { totalRecords, recordEventsToDownload, eventsToUpload, enrolmentsToUpload, samplesToUpload ->
            val (recordEventsToDownloadCount, isRecordEventsToDownloadLowerBound) = recordEventsToDownload
            SyncableCounts(
                totalRecords,
                recordEventsToDownload = recordEventsToDownloadCount,
                isRecordEventsToDownloadLowerBound,
                eventsToUpload,
                enrolmentsToUpload,
                samplesToUpload,
            )
        }.shareIn(
            appScope,
            SharingStarted.WhileSubscribed(),
            replay = 1,
        )
    }

    operator fun invoke(): Flow<SyncableCounts> = sharedSyncableCounts

}
