package com.simprints.id.services.sync.events.up

import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.events_sync.up.domain.EventUpSyncOperation.UpSyncState.COMPLETE
import com.simprints.eventsystem.events_sync.up.domain.EventUpSyncOperation.UpSyncState.FAILED
import com.simprints.eventsystem.events_sync.up.domain.EventUpSyncOperation.UpSyncState.RUNNING
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.data.prefs.settings.canSyncAllDataToSimprints
import com.simprints.id.data.prefs.settings.canSyncAnalyticsDataToSimprints
import com.simprints.id.data.prefs.settings.canSyncBiometricDataToSimprints
import com.simprints.id.services.sync.events.common.SYNC_LOG_TAG
import com.simprints.logging.Simber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class EventUpSyncHelperImpl(
    private val eventRepository: com.simprints.eventsystem.event.EventRepository,
    private val eventUpSyncScopeRepo: com.simprints.eventsystem.events_sync.up.EventUpSyncScopeRepository,
    private val timerHelper: TimeHelper,
    private val settingsPreferencesManager: SettingsPreferencesManager
) : EventUpSyncHelper {

    override suspend fun countForUpSync(operation: com.simprints.eventsystem.events_sync.up.domain.EventUpSyncOperation): Int =
        eventRepository.localCount(operation.projectId)

    override suspend fun upSync(
        scope: CoroutineScope,
        operation: com.simprints.eventsystem.events_sync.up.domain.EventUpSyncOperation
    ) =
        flow<EventUpSyncProgress> {
            var lastOperation = operation.copy()
            var count = 0
            try {
                eventRepository.uploadEvents(
                    projectId = operation.projectId,
                    canSyncAllDataToSimprints = settingsPreferencesManager.canSyncAllDataToSimprints(),
                    canSyncBiometricDataToSimprints = settingsPreferencesManager.canSyncBiometricDataToSimprints(),
                    canSyncAnalyticsDataToSimprints = settingsPreferencesManager.canSyncAnalyticsDataToSimprints()
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
        lastOperation: com.simprints.eventsystem.events_sync.up.domain.EventUpSyncOperation,
        count: Int
    ) {
        eventUpSyncScopeRepo.insertOrUpdate(lastOperation)
        this.emit(EventUpSyncProgress(lastOperation, count))
    }
}
