package com.simprints.eventsystem.sampledata

import com.simprints.eventsystem.event.domain.models.ConnectivitySnapshotEvent
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.sampledata.SampleDefaults.CREATED_AT
import com.simprints.core.tools.utils.SimNetworkUtils

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
