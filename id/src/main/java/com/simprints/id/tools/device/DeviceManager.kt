package com.simprints.id.tools.device

import androidx.lifecycle.LiveData
import com.simprints.id.exceptions.unexpected.RootedDeviceException

interface DeviceManager {

    val isConnectedUpdates: LiveData<Boolean>
    suspend fun isConnected(): Boolean

    @Throws(RootedDeviceException::class)
    fun checkIfDeviceIsRooted()
}
