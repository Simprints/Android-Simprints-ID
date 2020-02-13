package com.simprints.id.tools.device

import androidx.lifecycle.LiveData

class DeviceManagerImpl(private val connectivityHelper: ConnectivityHelper) : DeviceManager {


    override val isConnectedLiveData: LiveData<Boolean> by lazy {
        ConnectivityLiveData(connectivityHelper)
    }

    override fun isConnected() = connectivityHelper.isNetworkAvailable()
}
