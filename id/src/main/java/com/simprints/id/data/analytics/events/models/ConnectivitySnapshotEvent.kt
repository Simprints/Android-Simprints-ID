package com.simprints.id.data.analytics.events.models

import android.content.Context
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.NetworkUtils

class ConnectivitySnapshotEvent(
    val relativeStartTime: Long,
    val networkType: String?,
    val connections: List<NetworkUtils.Connection>) : Event(EventType.CONNECTIVITY_SNAPSHOT) {

    companion object {
        fun buildEvent(ctx: Context,
                       sessionEvents: SessionEvents,
                       timeHelper: TimeHelper): ConnectivitySnapshotEvent {

            return NetworkUtils(ctx).let {
                ConnectivitySnapshotEvent(
                    sessionEvents.nowRelativeToStartTime(timeHelper),
                    it.mobileNetworkType,
                    it.connectionsStates)
            }
        }
    }
}
