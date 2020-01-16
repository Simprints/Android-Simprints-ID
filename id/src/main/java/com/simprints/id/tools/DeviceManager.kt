package com.simprints.id.tools

import com.simprints.id.exceptions.unexpected.RootedDeviceException

interface DeviceManager {
    @Throws(RootedDeviceException::class)
    fun checkIfDeviceIsRooted()
}
