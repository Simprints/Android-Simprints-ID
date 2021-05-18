package com.simprints.eventsystem.event.domain.models

import android.net.NetworkInfo.DetailedState
import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.ConnectivitySnapshotEvent.Companion.EVENT_VERSION
import com.simprints.eventsystem.event.domain.models.EventType.CONNECTIVITY_SNAPSHOT
import com.simprints.id.sampledata.ConnectivitySnapshotEventSample
import com.simprints.id.sampledata.SampleDefaults.CREATED_AT
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_ENDED_AT
import com.simprints.id.sampledata.SampleDefaults.GUID1
import com.simprints.id.tools.utils.SimNetworkUtils.Connection
import org.junit.Test

class ConnectivitySnapshotEventTest {

    @Test
    fun create_ConnectivitySnapshotEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val connectionState = listOf(Connection("GPRS", DetailedState.CONNECTED))
        val event = ConnectivitySnapshotEventSample.getEvent(labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(CONNECTIVITY_SNAPSHOT)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CONNECTIVITY_SNAPSHOT)
            assertThat(networkType).isEqualTo("WIFI")
            assertThat(connections).isEqualTo(connectionState)
        }
    }
}
