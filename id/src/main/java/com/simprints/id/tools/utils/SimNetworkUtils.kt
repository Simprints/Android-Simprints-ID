package com.simprints.id.tools.utils;

import android.net.NetworkInfo;

interface SimNetworkUtils {

    class Connection(val type: String, val state: NetworkInfo.DetailedState)

    val mobileNetworkType: String?
    val connectionsStates: List<Connection>
    fun isConnected():Boolean
}
