package com.simprints.eventsystem.sampledata

import android.net.NetworkInfo
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
                "GPRS",
                NetworkInfo.DetailedState.CONNECTED
            )
        )
        return ConnectivitySnapshotEvent(
            CREATED_AT,
            "WIFI",
            connectionState,
            labels
        )
    }
}
