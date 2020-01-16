package com.simprints.id.tools

import android.content.Context
import com.scottyab.rootbeer.RootBeer

class DeviceManagerImpl(private val context: Context) : DeviceManager {

    override fun isDeviceRooted(): Boolean = RootBeer(context).isRootedWithoutBusyBoxCheck

}
