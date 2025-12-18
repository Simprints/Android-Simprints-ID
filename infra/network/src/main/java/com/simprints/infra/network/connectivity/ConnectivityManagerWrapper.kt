package com.simprints.infra.network.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import com.simprints.infra.network.ConnectivityTracker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ConnectivityManagerWrapper @Inject constructor(
    @ApplicationContext private val ctx: Context,
) : ConnectivityTracker {
    private val connectivityManager by lazy {
        ctx.getSystemService(ConnectivityManager::class.java)
    }

    /**
     * This method checks the active network connection status
     *
     * @return
     * True: if theres an active network &&
     *  has an active internet connection &&
     *  connectivity on this network was successfully validated
     * False: otherwise.
     */
    override fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        return capabilities != null &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    override fun observeIsConnected(): Flow<Boolean> = callbackFlow {
        val networkStatusCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onUnavailable() {
                trySend(false)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }

        trySend(isConnected())
        registerNetworkCallback(networkStatusCallback)

        awaitClose { unregisterNetworkCallback(networkStatusCallback) }
    }.distinctUntilChanged()

    private fun registerNetworkCallback(networkCallback: ConnectivityManager.NetworkCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val request = NetworkRequest.Builder().build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
        }
    }

    private fun unregisterNetworkCallback(networkCallback: ConnectivityManager.NetworkCallback) {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}
