package com.simprints.infra.events.sampledata

import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.infra.events.event.domain.models.ConnectivitySnapshotEvent
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT

object ConnectivitySnapshotEventSample : SampleEvent() {

    override fun getEvent(
        labels: EventLabels,
        isClosed: Boolean
    ): ConnectivitySnapshotEvent {
        val connectionState = listOf(
            SimNetworkUtils.Connection(
                SimNetworkUtils.ConnectionType.MOBILE,
                SimNetworkUtils.ConnectionState.CONNECTED
            )
        )
        return ConnectivitySnapshotEvent(
            CREATED_AT,
            connectionState,
            labels
        )
    }
}
