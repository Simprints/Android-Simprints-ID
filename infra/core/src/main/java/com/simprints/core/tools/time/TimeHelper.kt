package com.simprints.core.tools.time

import androidx.annotation.Keep
import kotlinx.coroutines.flow.Flow

@Keep
interface TimeHelper {
    fun ensureTrustworthiness()

    fun now(): Timestamp

    fun msBetweenNowAndTime(time: Timestamp): Long

    fun readableBetweenNowAndTime(date: Timestamp): String

    fun getCurrentDateAsString(): String

    fun todayInMillis(): Long

    fun tomorrowInMillis(): Long

    fun observeTickOncePerMinute(): Flow<Unit>
}
