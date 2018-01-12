package com.simprints.id.tools

import android.os.SystemClock


class TimeHelperImpl : TimeHelper {

    override fun msSinceBoot(): Long =
        SystemClock.elapsedRealtime()

}
