package com.simprints.infra.sync.devicestate

import com.simprints.infra.events.event.domain.models.scope.EventScopeType

/**
 * How much data is currently sitting on this device, and when each upload channel last succeeded.
 *
 * `null` means the underlying source could not be read, `0` means there is genuinely nothing.
 * Consumers must not collapse the two as they have very different consequences for a device that may be at risk.
 */
data class DeviceDataState(
    val projectId: String?,
    val recordCount: Int?,
    val pendingScopes: Map<EventScopeType, Int>?,
    val pendingEvents: Int?,
    val pendingEnrolments: Int?,
    val pendingSamples: Int?,
    val lastEventSyncAt: Long?, // Null when never synced on this install, or when the store could not be read.
    val lastSampleSyncAt: Long?, // Null when never synced on this install, or when the store could not be read.
)
