package com.simprints.id.tools.utils

import android.net.NetworkInfo
import androidx.annotation.Keep

interface SimNetworkUtils {

    @Keep
    data class Connection(val type: String, val state: NetworkInfo.DetailedState)

    val mobileNetworkType: String?
    val connectionsStates: List<Connection>
}
