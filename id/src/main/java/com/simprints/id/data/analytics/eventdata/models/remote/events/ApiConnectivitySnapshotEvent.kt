package com.simprints.id.data.analytics.eventdata.models.remote.events

import com.simprints.id.data.analytics.eventdata.models.domain.events.ConnectivitySnapshotEvent
import com.simprints.id.tools.utils.SimNetworkUtils

class ApiConnectivitySnapshotEvent(
    val relativeStartTime: Long,
    val networkType: String,
    val connections: List<ApiConnection>) : ApiEvent(ApiEventType.CONNECTIVITY_SNAPSHOT) {

    class ApiConnection(val type: String, val state: String) {
        constructor(connection: SimNetworkUtils.Connection)
            : this(connection.type, connection.state.toString())
    }

    constructor(connectivitySnapshotEvent: ConnectivitySnapshotEvent) :
        this(connectivitySnapshotEvent.relativeStartTime,
            connectivitySnapshotEvent.networkType,
            connectivitySnapshotEvent.connections.map { ApiConnection(it) })
}
