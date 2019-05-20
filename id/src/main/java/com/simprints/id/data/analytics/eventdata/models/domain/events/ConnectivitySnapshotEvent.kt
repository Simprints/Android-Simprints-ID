package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.SimNetworkUtils

@Keep
class ConnectivitySnapshotEvent(
    starTime: Long,
    val networkType: String,
    val connections: List<SimNetworkUtils.Connection>) : Event(EventType.CONNECTIVITY_SNAPSHOT, starTime = starTime) {

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
