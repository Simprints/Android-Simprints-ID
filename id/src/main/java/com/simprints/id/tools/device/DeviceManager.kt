package com.simprints.id.tools.device

import androidx.lifecycle.LiveData

interface DeviceManager {

    val isConnectedLiveData: LiveData<Boolean>
    suspend fun isConnected(): Boolean

}
