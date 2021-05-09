package com.simprints.id.sampledata

import android.net.NetworkInfo
import com.simprints.id.data.db.event.domain.models.ConnectivitySnapshotEvent
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.sampledata.SampleDefaults.CREATED_AT
import com.simprints.id.tools.utils.SimNetworkUtils

object ConnectivitySnapshotEventSample : SampleEvent() {

    override fun getEvent(
        sessionId: String,
        subjectId: String,
        isClosed: Boolean
    ): ConnectivitySnapshotEvent {
        val labels = EventLabels(sessionId = sessionId)
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
