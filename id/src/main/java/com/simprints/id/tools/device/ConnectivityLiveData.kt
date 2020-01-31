package com.simprints.id.tools.device

import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.LiveData


class ConnectivityLiveData(private val connectivityHelper: ConnectivityHelper) : LiveData<Boolean>() {

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network?) {
            postValue(true)
        }

        override fun onLost(network: Network?) {
            postValue(false)
        }

        override fun onUnavailable() {
            postValue(false)
        }
    }

    override fun onActive() {
        super.onActive()
        connectivityHelper.registerNetworkCallback(networkCallback)

        postValue(connectivityHelper.isNetworkAvailable())
    }

    override fun onInactive() {
        super.onInactive()
        connectivityHelper.unregisterNetworkCallback(networkCallback)
    }
}
