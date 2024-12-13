package com.simprints.infra.security.root

import com.simprints.infra.security.exceptions.RootedDeviceException

internal interface RootManager {
    /**
     * Check if the device is rooted
     * @throws RootedDeviceException
     */
    fun checkIfDeviceIsRooted()
}
