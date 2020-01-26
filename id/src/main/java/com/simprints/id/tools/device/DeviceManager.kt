package com.simprints.id.tools.device

import androidx.lifecycle.LiveData

interface DeviceManager {

    val isConnectedUpdates: LiveData<Boolean>
    suspend fun isConnected(): Boolean

}
