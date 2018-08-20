package com.simprints.id.tools

class TimeHelperImpl : TimeHelper {

    override fun now(): Long =
        System.currentTimeMillis()

    override fun msBetweenNowAndTime(time: Long): Long =
        now() - time
}
