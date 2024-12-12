package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.ConnectivitySnapshotEvent.ConnectivitySnapshotPayload

@Keep
internal data class ApiConnectivitySnapshotPayload(
    override val startTime: ApiTimestamp,
    val connections: List<ApiConnection>,
) : ApiEventPayload(startTime) {
    @Keep
    class ApiConnection(
        val type: String,
        val state: String,
    ) {
        constructor(connection: SimNetworkUtils.Connection) :
            this(connection.type.toString(), connection.state.toString())
    }

    constructor(domainPayload: ConnectivitySnapshotPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.connections.map { ApiConnection(it) },
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}
