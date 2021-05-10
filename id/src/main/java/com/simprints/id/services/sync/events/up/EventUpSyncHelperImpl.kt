package com.simprints.id.services.sync.events.up

import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.events_sync.up.EventUpSyncScopeRepository
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncOperation
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncOperation.UpSyncState.*
import com.simprints.id.services.sync.events.common.SYNC_LOG_TAG
import com.simprints.id.tools.time.TimeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class EventUpSyncHelperImpl(
    private val eventRepository: EventRepository,
    private val eventUpSyncScopeRepo: EventUpSyncScopeRepository,
    private val timerHelper: TimeHelper
) : EventUpSyncHelper {

    override suspend fun countForUpSync(operation: EventUpSyncOperation): Int =
        eventRepository.localCount(operation.projectId)

    override suspend fun upSync(scope: CoroutineScope, operation: EventUpSyncOperation) =
        flow<EventUpSyncProgress> {
            var lastOperation = operation.copy()
            var count = 0
            try {
                eventRepository.uploadEvents(operation.projectId).collect {
                    Timber.tag(SYNC_LOG_TAG).d("[UP_SYNC_HELPER] Uploading $it events")
                    count = it
                    lastOperation =
                        lastOperation.copy(lastState = RUNNING, lastSyncTime = timerHelper.now())
                    emitProgress(lastOperation, count)
                }

                lastOperation =
                    lastOperation.copy(lastState = COMPLETE, lastSyncTime = timerHelper.now())
                emitProgress(lastOperation, count)


            } catch (t: Throwable) {
                Timber.e(t)
                lastOperation =
                    lastOperation.copy(lastState = FAILED, lastSyncTime = timerHelper.now())
                emitProgress(lastOperation, count)
            }
        }

    private suspend fun FlowCollector<EventUpSyncProgress>.emitProgress(
        lastOperation: EventUpSyncOperation,
        count: Int
    ) {
        eventUpSyncScopeRepo.insertOrUpdate(lastOperation)
        this.emit(EventUpSyncProgress(lastOperation, count))
    }
}
