package com.simprints.eventsystem.event.remote.models

import androidx.annotation.Keep
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.eventsystem.event.domain.models.ConnectivitySnapshotEvent.ConnectivitySnapshotPayload


@Keep
data class ApiConnectivitySnapshotPayload(
    override val startTime: Long,
    override val version: Int,
    val connections: List<ApiConnection>) : ApiEventPayload(ApiEventPayloadType.ConnectivitySnapshot, version, startTime) {

    @Keep
    class ApiConnection(val type: String, val state: String) {
        constructor(connection: SimNetworkUtils.Connection)
            : this(connection.type, connection.state.toString())
    }

    constructor(domainPayload: ConnectivitySnapshotPayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.connections.map { ApiConnection(it) })
}
