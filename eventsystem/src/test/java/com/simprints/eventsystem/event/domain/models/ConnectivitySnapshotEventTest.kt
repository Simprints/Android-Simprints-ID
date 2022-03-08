package com.simprints.eventsystem.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.core.tools.utils.SimNetworkUtils.Connection
import com.simprints.eventsystem.event.domain.models.ConnectivitySnapshotEvent.Companion.EVENT_VERSION
import com.simprints.eventsystem.event.domain.models.EventType.CONNECTIVITY_SNAPSHOT
import com.simprints.eventsystem.sampledata.ConnectivitySnapshotEventSample
import com.simprints.eventsystem.sampledata.SampleDefaults.CREATED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_ENDED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class ConnectivitySnapshotEventTest {

    @Test
    fun create_ConnectivitySnapshotEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val connectionState = listOf(Connection(
            SimNetworkUtils.ConnectionType.MOBILE,
            SimNetworkUtils.ConnectionState.CONNECTED))
        val event = ConnectivitySnapshotEventSample.getEvent(labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(CONNECTIVITY_SNAPSHOT)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CONNECTIVITY_SNAPSHOT)
            assertThat(connections).isEqualTo(connectionState)
        }
    }

    @Test
    fun `test ConnectivitySnapshotPayload buildEvent`() {
        val simNetworkUtils: SimNetworkUtils = mockk(relaxed = true)
        val timeHelper: TimeHelper = mockk(relaxed = true)
        val payload =
            ConnectivitySnapshotEvent.ConnectivitySnapshotPayload.buildEvent(
                simNetworkUtils,
                timeHelper
            )

        verify { timeHelper.now() }
        verify { simNetworkUtils.connectionsStates }
    }
}
