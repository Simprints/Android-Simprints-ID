package com.simprints.core.tools.utils

import androidx.annotation.Keep

@Keep
interface SimNetworkUtils {
    @Keep
    enum class ConnectionType {
        WIFI,
        MOBILE,
    }

    @Keep
    enum class ConnectionState {
        CONNECTED,
        DISCONNECTED,
    }

    @Keep
    data class Connection(
        val type: ConnectionType,
        val state: ConnectionState,
    )

    val connectionsStates: List<Connection>
}
