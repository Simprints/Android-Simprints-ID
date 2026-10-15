package com.simprints.infra.sync.devicestate.remote

import androidx.annotation.Keep
import com.simprints.core.api.ApiTimestamp
import com.simprints.core.api.fromDomainToApi
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.sync.devicestate.DeviceDataState
import kotlinx.serialization.Serializable

/**
 * `null` fields are omitted from the request: a source that could not be read must never be reported as `0`.
 */
@Keep
@Serializable
internal data class ApiDeviceDataState(
    val scopesToUpload: ApiScopesToUpload?,
    val enrolmentsToUpload: Int?,
    val samplesToUpload: Int?,
    val lastEventSyncAt: ApiTimestamp?,
    val lastEventSyncFailureReason: String?,
    val lastSampleSyncAt: ApiTimestamp?,
    val lastSampleSyncFailureReason: String?,
)

@Keep
@Serializable
internal data class ApiScopesToUpload(
    val sessionCount: Int,
    val eventUpSyncCount: Int,
    val eventDownSyncCount: Int,
    val sampleUpSyncCount: Int,
    val deviceCount: Int,
)

internal fun DeviceDataState.toApi() = ApiDeviceDataState(
    scopesToUpload = pendingScopes?.toApi(),
    enrolmentsToUpload = pendingEnrolments,
    samplesToUpload = pendingSamples,
    lastEventSyncAt = lastEventSyncAt?.fromDomainToApi(),
    lastEventSyncFailureReason = lastEventSyncFailure?.name,
    lastSampleSyncAt = lastSampleSyncAt?.fromDomainToApi(),
    lastSampleSyncFailureReason = lastSampleSyncFailure?.name,
)

private fun Map<EventScopeType, Int>.toApi(): ApiScopesToUpload = ApiScopesToUpload(
    sessionCount = this[EventScopeType.SESSION] ?: 0,
    eventUpSyncCount = this[EventScopeType.UP_SYNC] ?: 0,
    eventDownSyncCount = this[EventScopeType.DOWN_SYNC] ?: 0,
    sampleUpSyncCount = this[EventScopeType.SAMPLE_UP_SYNC] ?: 0,
    deviceCount = this[EventScopeType.DEVICE] ?: 0,
)
