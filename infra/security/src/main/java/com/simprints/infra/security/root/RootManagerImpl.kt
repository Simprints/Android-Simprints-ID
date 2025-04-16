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
        // TODO Revert the changes to this file
        // PenTest 2025, the root check is removed because the PenTesters are using root devices, and it prevents them from proceeding
        // further

        // if (RootBeer(ctx).isRooted) throw RootedDeviceException()
    }
}
