package com.simprints.id.tools.utils

import android.content.Context
import android.net.ConnectivityManager
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.core.tools.utils.SimNetworkUtils.Connection
import com.simprints.core.tools.utils.SimNetworkUtils.ConnectionType
import android.net.NetworkCapabilities


open class SimNetworkUtilsImpl(val ctx: Context) : SimNetworkUtils {

    private val connectivityManager =
        ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val activeNetwork = connectivityManager.activeNetwork
    private val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

    /**
     * This property should contain two states for mobile and wifi connections
     */
    override var connectionsStates: List<Connection> =
        arrayListOf<Connection>().apply {
            add(createMobileDataConnection())
            add(createWifiConnection())
        }

    private fun createWifiConnection() =
        Connection(ConnectionType.WIFI, networkStatus(NetworkCapabilities.TRANSPORT_WIFI))

    private fun createMobileDataConnection() =
        Connection(ConnectionType.MOBILE, networkStatus(NetworkCapabilities.TRANSPORT_CELLULAR))

    private fun networkStatus(transportType: Int): SimNetworkUtils.ConnectionState =
        if (networkCapabilities?.hasTransport(transportType) == true) {
            SimNetworkUtils.ConnectionState.CONNECTED
        } else {
            SimNetworkUtils.ConnectionState.DISCONNECTED
        }
}
