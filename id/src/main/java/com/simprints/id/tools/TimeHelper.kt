package com.simprints.id.tools

import java.util.concurrent.TimeUnit

interface TimeHelper {

    fun now(): Long
    fun nowMinus(duration: Long, unit: TimeUnit = TimeUnit.MILLISECONDS): Long
    fun msBetweenNowAndTime(time: Long): Long
}
