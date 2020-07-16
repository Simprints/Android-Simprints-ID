package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.EventLabel.SessionIdLabel
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.SimNetworkUtils
import java.util.*

@Keep
class ConnectivitySnapshotEvent(
    createdAt: Long,
    networkType: String,
    connections: List<SimNetworkUtils.Connection>,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    ConnectivitySnapshotPayload(createdAt, DEFAULT_EVENT_VERSION, networkType, connections)) {

    @Keep
    class ConnectivitySnapshotPayload(
        createdAt: Long,
        eventVersion: Int,
        val networkType: String,
        val connections: List<SimNetworkUtils.Connection>
    ) : EventPayload(EventPayloadType.CONNECTIVITY_SNAPSHOT, eventVersion, createdAt) {

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
}
