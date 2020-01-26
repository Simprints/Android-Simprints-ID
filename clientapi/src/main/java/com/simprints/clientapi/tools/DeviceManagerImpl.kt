package com.simprints.clientapi.tools

import android.content.Context
import com.scottyab.rootbeer.RootBeer
import com.simprints.clientapi.exceptions.RootedDeviceException

class DeviceManagerImpl(private val context: Context) : DeviceManager {

    override fun checkIfDeviceIsRooted() {
        val isDeviceRooted =  RootBeer(context).isRootedWithoutBusyBoxCheck
        if (isDeviceRooted)
            throw RootedDeviceException()
    }

}
