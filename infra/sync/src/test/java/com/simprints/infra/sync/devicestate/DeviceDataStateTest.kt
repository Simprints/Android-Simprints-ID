package com.simprints.infra.sync.devicestate

import com.google.common.truth.Truth.*
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import org.junit.Test

class DeviceDataStateTest {
    @Test
    fun `has no data to upload when sessions, enrolments and samples are all zero`() {
        val state = createDeviceDataState(pendingSessions = 0, pendingEnrolments = 0, pendingSamples = 0)

        assertThat(state.hasDataToUpload).isFalse()
    }

    @Test
    fun `has data to upload when closed sessions are left`() {
        val state = createDeviceDataState(pendingSessions = 1, pendingEnrolments = 0, pendingSamples = 0)

        assertThat(state.hasDataToUpload).isTrue()
    }

    @Test
    fun `has data to upload when enrolments are left`() {
        val state = createDeviceDataState(pendingSessions = 0, pendingEnrolments = 1, pendingSamples = 0)

        assertThat(state.hasDataToUpload).isTrue()
    }

    @Test
    fun `has data to upload when samples are left`() {
        val state = createDeviceDataState(pendingSessions = 0, pendingEnrolments = 0, pendingSamples = 1)

        assertThat(state.hasDataToUpload).isTrue()
    }

    @Test
    fun `has data to upload when the counts could not be read`() {
        val state = createDeviceDataState(pendingSessions = null, pendingEnrolments = null, pendingSamples = null)

        assertThat(state.hasDataToUpload).isTrue()
    }

    private fun createDeviceDataState(
        pendingSessions: Int?,
        pendingEnrolments: Int?,
        pendingSamples: Int?,
    ) = DeviceDataState(
        projectId = "projectId",
        recordCount = 0,
        pendingScopes = pendingSessions?.let { mapOf(EventScopeType.SESSION to it) },
        pendingEvents = 0,
        pendingEnrolments = pendingEnrolments,
        pendingSamples = pendingSamples,
        lastEventSyncAt = null,
        lastEventSyncFailure = null,
        lastSampleSyncAt = null,
        lastSampleSyncFailure = null,
    )
}
