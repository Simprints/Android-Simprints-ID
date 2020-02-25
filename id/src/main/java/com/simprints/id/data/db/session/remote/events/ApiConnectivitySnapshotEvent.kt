package com.simprints.id.data.db.session.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.ConnectivitySnapshotEvent
import com.simprints.id.tools.utils.SimNetworkUtils

@Keep
class ApiConnectivitySnapshotEvent(
    val relativeStartTime: Long,
    val networkType: String,
    val connections: List<ApiConnection>) : ApiEvent(ApiEventType.CONNECTIVITY_SNAPSHOT) {

    @Keep
    class ApiConnection(val type: String, val state: String) {
        constructor(connection: SimNetworkUtils.Connection)
            : this(connection.type, connection.state.toString())
    }

    constructor(connectivitySnapshotEvent: ConnectivitySnapshotEvent) :
        this(connectivitySnapshotEvent.relativeStartTime ?: 0,
            connectivitySnapshotEvent.networkType,
            connectivitySnapshotEvent.connections.map { ApiConnection(it) })
}
