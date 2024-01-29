package com.simprints.core.tools.time

import androidx.annotation.Keep
import java.util.*
import java.util.concurrent.TimeUnit

@Keep
interface TimeHelper {

    fun ensureTrustworthiness()

    // TODO rename to now() when all usage of current implementation are moved to this method
    fun nowTimestamp(): Timestamp

    @Deprecated("Use nowTimestamp() instead")
    fun now(): Long

    fun msBetweenNowAndTime(time: Long): Long
    fun readableBetweenNowAndTime(date: Date): String
    fun getCurrentDateAsString(): String
    fun todayInMillis(): Long
    fun tomorrowInMillis(): Long
}
