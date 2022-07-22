package com.simprints.infra.security.root

import android.content.Context
import com.scottyab.rootbeer.RootBeer
import com.simprints.infra.security.exceptions.RootedDeviceException

class RootManagerImpl(private val ctx: Context) : RootManager {

    override fun checkIfDeviceIsRooted() {
        if (RootBeer(ctx).isRooted) throw RootedDeviceException()
    }
}
