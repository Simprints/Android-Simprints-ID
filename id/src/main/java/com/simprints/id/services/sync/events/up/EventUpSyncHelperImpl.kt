package com.simprints.id.services.sync.events.up

import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.events_sync.up.EventUpSyncScopeRepository
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncOperation
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncOperation.UpSyncState.*
import com.simprints.id.services.sync.events.common.SYNC_LOG_TAG
import com.simprints.id.tools.time.TimeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.collect
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class EventUpSyncHelperImpl(
    private val eventRepository: EventRepository,
    private val eventUpSyncScopeRepo: EventUpSyncScopeRepository,
    private val timerHelper: TimeHelper
) : EventUpSyncHelper {

    override suspend fun countForUpSync(operation: EventUpSyncOperation): Int =
        eventRepository.countEventsToUpload(operation.queryEvent)

    override suspend fun upSync(scope: CoroutineScope, operation: EventUpSyncOperation): ReceiveChannel<EventUpSyncProgress> =
        scope.produce {
            var lastOperation = operation.copy()
            var count = 0
            try {
                eventRepository.uploadEvents(operation.queryEvent).collect {
                    Timber.tag(SYNC_LOG_TAG).d("[UP_SYNC_HELPER] Uploading $it events")
                    count += it
                    lastOperation = lastOperation.copy(lastState = RUNNING, lastSyncTime = timerHelper.now())
                    emitProgress(lastOperation, count)
                }

                lastOperation = lastOperation.copy(lastState = COMPLETE, lastSyncTime = timerHelper.now())
                emitProgress(lastOperation, count)
                close()

            } catch (t: Throwable) {
                Timber.d(t)
                lastOperation = lastOperation.copy(lastState = FAILED, lastSyncTime = timerHelper.now())
                emitProgress(lastOperation, count)
                close()
            }
        }

    private suspend fun ProducerScope<EventUpSyncProgress>.emitProgress(lastOperation: EventUpSyncOperation, count: Int) {
        if (!this.isClosedForSend) {
            eventUpSyncScopeRepo.insertOrUpdate(lastOperation)
            this.send(EventUpSyncProgress(lastOperation, count))
        }
    }
}
