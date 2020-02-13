package com.simprints.clientapi.tools

import com.simprints.clientapi.exceptions.RootedDeviceException

interface DeviceManager {
    @Throws(RootedDeviceException::class)
    fun checkIfDeviceIsRooted()
}
