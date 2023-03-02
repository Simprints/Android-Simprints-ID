package com.simprints.id.services.sync.events.up

import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.EventRepository
import com.simprints.eventsystem.events_sync.up.EventUpSyncScopeRepository
import com.simprints.eventsystem.events_sync.up.domain.EventUpSyncOperation
import com.simprints.eventsystem.events_sync.up.domain.EventUpSyncOperation.UpSyncState.*
import com.simprints.id.services.sync.events.common.SYNC_LOG_TAG
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.*
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class EventUpSyncHelperImpl @Inject constructor(
    private val eventRepository: EventRepository,
    private val eventUpSyncScopeRepo: EventUpSyncScopeRepository,
    private val timerHelper: TimeHelper,
    private val configManager: ConfigManager,
) : EventUpSyncHelper {

    override suspend fun countForUpSync(operation: EventUpSyncOperation): Int =
        eventRepository.localCount(operation.projectId)

    override fun upSync(
        scope: CoroutineScope,
        operation: EventUpSyncOperation
    ) =
        flow {
            val config = configManager.getProjectConfiguration()
            var lastOperation = operation.copy()
            var count = 0
            try {
                eventRepository.uploadEvents(
                    projectId = operation.projectId,
                    canSyncAllDataToSimprints = config.canSyncAllDataToSimprints(),
                    canSyncBiometricDataToSimprints = config.canSyncBiometricDataToSimprints(),
                    canSyncAnalyticsDataToSimprints = config.canSyncAnalyticsDataToSimprints()
                ).collect {
                    Simber.tag(SYNC_LOG_TAG).d("[UP_SYNC_HELPER] Uploading $it events")
                    count = it
                    lastOperation =
                        lastOperation.copy(lastState = RUNNING, lastSyncTime = timerHelper.now())
                    emitProgress(lastOperation, count)
                }

                lastOperation =
                    lastOperation.copy(lastState = COMPLETE, lastSyncTime = timerHelper.now())
                emitProgress(lastOperation, count)


            } catch (t: Throwable) {
                Simber.e(t)
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
