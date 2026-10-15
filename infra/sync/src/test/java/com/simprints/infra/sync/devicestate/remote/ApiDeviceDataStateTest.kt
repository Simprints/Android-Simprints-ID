package com.simprints.infra.sync.devicestate.remote

import com.google.common.truth.Truth.*
import com.simprints.core.api.ApiTimestamp
import com.simprints.core.domain.sync.SyncFailureReason
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.serialization.SimJson
import com.simprints.infra.sync.devicestate.DeviceDataState
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.Test

class ApiDeviceDataStateTest {
    @Test
    fun `maps every field of a fully readable state`() {
        val api = fullState().toApi()

        assertThat(api).isEqualTo(
            ApiDeviceDataState(
                scopesToUpload = ApiScopesToUpload(
                    sessionCount = 1,
                    eventUpSyncCount = 2,
                    eventDownSyncCount = 3,
                    sampleUpSyncCount = 4,
                    deviceCount = 5,
                ),
                enrolmentsToUpload = 6,
                samplesToUpload = 7,
                lastEventSyncAt = ApiTimestamp(unixMs = 100L, isUnixMsTrustworthy = true, elapsedSinceBoot = 10L),
                lastEventSyncFailureReason = "BACKEND_MAINTENANCE",
                lastSampleSyncAt = ApiTimestamp(unixMs = 200L, isUnixMsTrustworthy = false, elapsedSinceBoot = null),
                lastSampleSyncFailureReason = "UNKNOWN",
            ),
        )
    }

    @Test
    fun `missing scope types are reported as zero`() {
        val scopes = fullState().copy(pendingScopes = emptyMap()).toApi().scopesToUpload

        assertThat(scopes).isEqualTo(ApiScopesToUpload(0, 0, 0, 0, 0))
    }

    @Test
    fun `unreadable sources are omitted from the request rather than sent as zero`() {
        val state = DeviceDataState(
            projectId = null,
            recordCount = null,
            pendingScopes = null,
            pendingEvents = null,
            pendingEnrolments = null,
            pendingSamples = null,
            lastEventSyncAt = null,
            lastEventSyncFailure = null,
            lastSampleSyncAt = null,
            lastSampleSyncFailure = null,
        )

        val json = SimJson.encodeToJsonElement(ApiDeviceDataState.serializer(), state.toApi()).jsonObject

        assertThat(json).isEqualTo(JsonObject(emptyMap()))
    }

    private fun fullState() = DeviceDataState(
        projectId = "projectId",
        recordCount = 99,
        pendingScopes = mapOf(
            EventScopeType.SESSION to 1,
            EventScopeType.UP_SYNC to 2,
            EventScopeType.DOWN_SYNC to 3,
            EventScopeType.SAMPLE_UP_SYNC to 4,
            EventScopeType.DEVICE to 5,
        ),
        pendingEvents = 50,
        pendingEnrolments = 6,
        pendingSamples = 7,
        lastEventSyncAt = Timestamp(ms = 100L, isTrustworthy = true, msSinceBoot = 10L),
        lastEventSyncFailure = SyncFailureReason.BACKEND_MAINTENANCE,
        lastSampleSyncAt = Timestamp(ms = 200L),
        lastSampleSyncFailure = SyncFailureReason.UNKNOWN,
    )
}
