package com.simprints.core.tools.time

import androidx.annotation.Keep
import java.util.Date

@Keep
interface TimeHelper {

    fun ensureTrustworthiness()

    fun now(): Timestamp

    fun msBetweenNowAndTime(time: Long): Long
    fun readableBetweenNowAndTime(date: Date): String
    fun getCurrentDateAsString(): String
    fun todayInMillis(): Long
    fun tomorrowInMillis(): Long
}
