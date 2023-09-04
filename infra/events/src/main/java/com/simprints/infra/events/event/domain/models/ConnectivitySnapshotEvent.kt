package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.CONNECTIVITY_SNAPSHOT
import java.util.UUID

@Keep
data class ConnectivitySnapshotEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: ConnectivitySnapshotPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        connections: List<SimNetworkUtils.Connection>,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        ConnectivitySnapshotPayload(createdAt, EVENT_VERSION, connections),
        CONNECTIVITY_SNAPSHOT
    )

    override fun getTokenizedFields(): Map<TokenKeyType, String> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, String>) = this // No tokenized fields

    @Keep
    data class ConnectivitySnapshotPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val connections: List<SimNetworkUtils.Connection>,
        override val type: EventType = CONNECTIVITY_SNAPSHOT,
        override val endedAt: Long = 0
    ) : EventPayload() {

        companion object {
            fun buildEvent(
                simNetworkUtils: SimNetworkUtils,
                timeHelper: TimeHelper
            ): ConnectivitySnapshotEvent {

                return ConnectivitySnapshotEvent(
                    timeHelper.now(),
                    simNetworkUtils.connectionsStates
                )
            }
        }
    }

    companion object {
        const val EVENT_VERSION = 2
    }
}
