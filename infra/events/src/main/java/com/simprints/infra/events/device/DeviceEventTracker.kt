package com.simprints.infra.events.device

import com.simprints.core.ExternalScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.DeviceConfigurationUpdatedEvent
import com.simprints.infra.events.event.domain.models.scope.EventScopeEndCause
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class DeviceEventTracker @Inject constructor(
    private val eventRepository: EventRepository,
    private val timeHelper: TimeHelper,
    @param:ExternalScope private val externalScope: CoroutineScope,
) {
    suspend fun trackDeviceConfigurationUpdatedEvent(
        deviceConfiguration: DeviceConfiguration,
        isLocalChange: Boolean,
    ) = externalScope.launch {
        val eventScope = eventRepository.createEventScope(
            type = EventScopeType.DEVICE,
            scopeId = null,
        )
        eventRepository.addOrUpdateEvent(
            scope = eventScope,
            event = DeviceConfigurationUpdatedEvent(
                createdAt = timeHelper.now(),
                language = deviceConfiguration.language,
                downSyncModules = deviceConfiguration.selectedModules.ifEmpty { null },
                sourceUpdate = if (isLocalChange) {
                    DeviceConfigurationUpdatedEvent.DeviceConfigurationUpdateSource.LOCAL
                } else {
                    DeviceConfigurationUpdatedEvent.DeviceConfigurationUpdateSource.REMOTE
                },
            ),
        )
        eventRepository.closeEventScope(eventScope, EventScopeEndCause.WORKFLOW_ENDED)
    }
}
