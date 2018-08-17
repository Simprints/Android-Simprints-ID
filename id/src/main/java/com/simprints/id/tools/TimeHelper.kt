package com.simprints.id.tools

interface TimeHelper {

    fun msSinceBoot(): Long
    fun msBetweenNowAndTime(time: Long): Long
}
