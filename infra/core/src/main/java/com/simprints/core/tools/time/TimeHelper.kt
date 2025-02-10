package com.simprints.core.tools.time

import androidx.annotation.Keep

@Keep
interface TimeHelper {
    fun ensureTrustworthiness()

    fun now(): Timestamp

    fun msBetweenNowAndTime(time: Timestamp): Long

    fun readableBetweenNowAndTime(date: Timestamp): String

    fun getCurrentDateAsString(): String

    fun todayInMillis(): Long

    fun tomorrowInMillis(): Long
}
