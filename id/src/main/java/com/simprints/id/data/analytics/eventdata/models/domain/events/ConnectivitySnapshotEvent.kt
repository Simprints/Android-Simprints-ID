package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.SimNetworkUtils

class ConnectivitySnapshotEvent(
    val relativeStartTime: Long,
    val networkType: String,
    val connections: List<SimNetworkUtils.Connection>) : Event(EventType.CONNECTIVITY_SNAPSHOT) {

    companion object {
        fun buildEvent(simNetworkUtils: SimNetworkUtils,
                       sessionEvents: SessionEvents,
                       timeHelper: TimeHelper): ConnectivitySnapshotEvent {

            return simNetworkUtils.let {
                ConnectivitySnapshotEvent(
                    sessionEvents.timeRelativeToStartTime(timeHelper.now()),
                    it.mobileNetworkType ?: "",
                    it.connectionsStates)
            }
        }
    }
}
