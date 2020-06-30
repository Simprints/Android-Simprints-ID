package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.ConnectivitySnapshotEvent
import com.simprints.id.data.db.event.domain.events.ConnectivitySnapshotEvent.ConnectivitySnapshotPayload
import com.simprints.id.tools.utils.SimNetworkUtils


@Keep
class ApiConnectivitySnapshotEvent(domainEvent: ConnectivitySnapshotEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.fromDomainToApi(),
        domainEvent.payload.fromDomainToApi()) {


    @Keep
    class ApiConnectivitySnapshotPayload(
        createdAt: Long,
        eventVersion: Int,
        val networkType: String,
        val connections: List<ApiConnection>) : ApiEventPayload(ApiEventPayloadType.CONNECTIVITY_SNAPSHOT, eventVersion, createdAt) {

        @Keep
        class ApiConnection(val type: String, val state: String) {
            constructor(connection: SimNetworkUtils.Connection)
                : this(connection.type, connection.state.toString())
        }

        constructor(domainPayload: ConnectivitySnapshotPayload) :
            this(domainPayload.createdAt,
                domainPayload.eventVersion,
                domainPayload.networkType,
                domainPayload.connections.map { ApiConnection(it) })
    }
}
