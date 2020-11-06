package com.simprints.id.tools.time

import java.util.*
import java.util.concurrent.TimeUnit

interface TimeHelper {

    fun now(): Long
    fun nowMinus(duration: Long, unit: TimeUnit = TimeUnit.MILLISECONDS): Long
    fun msBetweenNowAndTime(time: Long): Long
    fun readableBetweenNowAndTime(date: Date): String
    fun getCurrentDateAsString(): String
    fun todayInMillis(): Long
    fun tomorrowInMillis(): Long
}
