package com.simprints.id.tools

import java.util.concurrent.TimeUnit

class TimeHelperImpl : TimeHelper {

    override fun now(): Long =
        System.currentTimeMillis()

    override fun nowMinus(duration: Long, unit: TimeUnit): Long =
        now() - unit.toMillis(duration)

    override fun msBetweenNowAndTime(time: Long): Long =
        now() - time
}
