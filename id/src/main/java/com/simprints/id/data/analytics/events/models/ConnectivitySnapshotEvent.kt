package com.simprints.id.data.analytics.events.models

import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.SimNetworkUtils

class ConnectivitySnapshotEvent(
    val relativeStartTime: Long,
    val networkType: String?,
    val connections: List<SimNetworkUtils.Connection>) : Event(EventType.CONNECTIVITY_SNAPSHOT) {

    companion object {
        fun buildEvent(simNetworkUtils: SimNetworkUtils,
                       sessionEvents: SessionEvents,
                       timeHelper: TimeHelper): ConnectivitySnapshotEvent {

            return simNetworkUtils.let {
                ConnectivitySnapshotEvent(
                    sessionEvents.nowRelativeToStartTime(timeHelper),
                    it.mobileNetworkType,
                    it.connectionsStates)
            }
        }
    }
}
