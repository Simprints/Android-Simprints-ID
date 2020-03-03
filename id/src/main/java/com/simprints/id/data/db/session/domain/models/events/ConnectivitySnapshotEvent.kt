package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.SimNetworkUtils

@Keep
class ConnectivitySnapshotEvent(
    starTime: Long,
    val networkType: String,
    val connections: List<SimNetworkUtils.Connection>) : Event(EventType.CONNECTIVITY_SNAPSHOT, startTime = starTime) {

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
