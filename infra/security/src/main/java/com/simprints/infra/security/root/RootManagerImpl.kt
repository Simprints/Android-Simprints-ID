package com.simprints.infra.security.root

import android.content.Context
import com.scottyab.rootbeer.RootBeer
import com.simprints.infra.security.exceptions.RootedDeviceException
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class RootManagerImpl @Inject constructor(
    @ApplicationContext private val ctx: Context,
) : RootManager {
    override fun checkIfDeviceIsRooted() {
        if (RootBeer(ctx).isRooted) throw RootedDeviceException()
    }
}
