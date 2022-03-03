package com.simprints.core.tools.utils

import android.net.NetworkInfo
import androidx.annotation.Keep

interface SimNetworkUtils {

    @Keep
    data class Connection(val type: String = "", val state: NetworkInfo.DetailedState? = null)

    @Keep
    data class Connection(val type: ConnectionType, val state: ConnectionState)

    val connectionsStates: List<Connection>

}
