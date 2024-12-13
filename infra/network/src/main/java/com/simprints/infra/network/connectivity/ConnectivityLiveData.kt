package com.simprints.infra.network.connectivity

import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.LiveData

internal class ConnectivityLiveData(
    private val connectivityManagerWrapper: ConnectivityManagerWrapper,
) : LiveData<Boolean>() {
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            postValue(true)
        }

        override fun onLost(network: Network) {
            postValue(false)
        }

        override fun onUnavailable() {
            postValue(false)
        }
    }

    override fun onActive() {
        super.onActive()
        connectivityManagerWrapper.registerNetworkCallback(networkCallback)

        postValue(connectivityManagerWrapper.isNetworkAvailable())
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManagerWrapper.unregisterNetworkCallback(networkCallback)
    }
}
