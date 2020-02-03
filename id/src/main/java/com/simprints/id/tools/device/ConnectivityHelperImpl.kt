package com.simprints.id.tools.device

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.os.Build


class ConnectivityHelperImpl(private val ctx: Context) : ConnectivityHelper {

    private val connectivityManager by lazy {
        ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    override fun isNetworkAvailable(): Boolean {
        val networkInfo = connectivityManager.activeNetworkInfo
        var isAvailable = false
        if (networkInfo != null && networkInfo.isConnected) {
            // Network is present and connected
            isAvailable = true
        }
        return isAvailable
    }

    override fun registerNetworkCallback(networkCallback: ConnectivityManager.NetworkCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val builder = NetworkRequest.Builder()
            connectivityManager.registerNetworkCallback(builder.build(), networkCallback)
        }
    }

    override fun unregisterNetworkCallback(networkCallback: ConnectivityManager.NetworkCallback) {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}
