package com.simprints.id.tools.device

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import com.simprints.core.tools.extentions.resumeSafely
import kotlinx.coroutines.suspendCancellableCoroutine

class ConnectionCoroutine(private val connectivityManager: ConnectivityManager) {

    suspend fun isConnected(): Boolean = suspendCancellableCoroutine {

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network?) {
                it.resumeSafely(true)
                connectivityManager.unregisterNetworkCallback(this)
            }

            override fun onLost(network: Network?) {
                it.resumeSafely(false)
                connectivityManager.unregisterNetworkCallback(this)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val builder = NetworkRequest.Builder()
            connectivityManager.registerNetworkCallback(builder.build(), networkCallback)
        }
    }
}
