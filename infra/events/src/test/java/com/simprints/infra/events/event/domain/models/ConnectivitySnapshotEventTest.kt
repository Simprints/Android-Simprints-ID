package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.core.tools.utils.SimNetworkUtils.Connection
import com.simprints.infra.events.event.domain.models.ConnectivitySnapshotEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.event.domain.models.EventType.CONNECTIVITY_SNAPSHOT
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import org.junit.Test

class ConnectivitySnapshotEventTest {
    @Test
    fun create_ConnectivitySnapshotEvent() {
        val connectionState = listOf(
            Connection(
                SimNetworkUtils.ConnectionType.MOBILE,
                SimNetworkUtils.ConnectionState.CONNECTED,
            ),
        )
        val event = ConnectivitySnapshotEvent(
            CREATED_AT,
            connectionState,
        )

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(CONNECTIVITY_SNAPSHOT)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CONNECTIVITY_SNAPSHOT)
            assertThat(connections).isEqualTo(connectionState)
        }
    }
}
