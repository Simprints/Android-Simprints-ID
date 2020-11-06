package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventType.CONNECTIVITY_SNAPSHOT
import com.simprints.id.tools.time.TimeHelper
import com.simprints.id.tools.utils.SimNetworkUtils
import java.util.*

@Keep
data class ConnectivitySnapshotEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: ConnectivitySnapshotPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        networkType: String,
        connections: List<SimNetworkUtils.Connection>,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        ConnectivitySnapshotPayload(createdAt, EVENT_VERSION, networkType, connections),
        CONNECTIVITY_SNAPSHOT)

    @Keep
    data class ConnectivitySnapshotPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val networkType: String,
        val connections: List<SimNetworkUtils.Connection>,
        override val type: EventType = CONNECTIVITY_SNAPSHOT,
        override val endedAt: Long = 0
    ) : EventPayload() {

        companion object {
            fun buildEvent(simNetworkUtils: SimNetworkUtils,
                           timeHelper: TimeHelper): ConnectivitySnapshotEvent {

                return simNetworkUtils.let {
                    ConnectivitySnapshotEvent(
                        timeHelper.now(),
                        it.mobileNetworkType ?: "",
                        it.connectionsStates)
                }
            }
        }
    }

    companion object {
        const val EVENT_VERSION = 1
    }
}
