package com.simprints.id.tools.device

import android.net.ConnectivityManager

interface ConnectivityHelper {

    fun isNetworkAvailable(): Boolean

    fun registerNetworkCallback(networkCallback: ConnectivityManager.NetworkCallback)
    fun unregisterNetworkCallback(networkCallback: ConnectivityManager.NetworkCallback)
}
