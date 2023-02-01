package com.simprints.id.tools.device

import androidx.lifecycle.LiveData
import javax.inject.Inject

class DeviceManagerImpl @Inject constructor(private val connectivityHelper: ConnectivityHelper) :
    DeviceManager, com.simprints.feature.dashboard.main.sync.DeviceManager {


    override val isConnectedLiveData: LiveData<Boolean> by lazy {
        ConnectivityLiveData(connectivityHelper)
    }

    override fun isConnected() = connectivityHelper.isNetworkAvailable()
}
