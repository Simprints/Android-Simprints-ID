package com.simprints.core.tools.time

import android.text.format.DateUtils.FORMAT_SHOW_DATE
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.text.format.DateUtils.getRelativeTimeSpanString
import com.lyft.kronos.KronosClock
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class KronosTimeHelperImpl @Inject constructor(private val clock: KronosClock) : TimeHelper {

    init {
        clock.syncInBackground()
    }

    override fun ensureTrustworthiness() {
        clock.sync()
    }

    override fun nowTimestamp(): Timestamp = clock.getCurrentTime().let {
        Timestamp(
            ms = it.posixTimeMs,
            isTrustworthy = it.timeSinceLastNtpSyncMs != null,
            msSinceBoot = clock.getElapsedTimeMs()
        )
    }

    @Deprecated("Use nowTimestamp() instead")
    override fun now(): Long = clock.getCurrentTimeMs()

    override fun msBetweenNowAndTime(time: Long): Long = nowTimestamp().ms - time

    override fun readableBetweenNowAndTime(date: Date): String =
        getRelativeTimeSpanString(date.time, nowTimestamp().ms, MINUTE_IN_MILLIS, FORMAT_SHOW_DATE).toString()

    override fun getCurrentDateAsString(): String {
        val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
        return dateFormat.format(Date(nowTimestamp().ms))
    }

    override fun todayInMillis(): Long = Calendar.getInstance().run {
        timeInMillis = nowTimestamp().ms
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        timeInMillis
    }

    override fun tomorrowInMillis(): Long = Calendar.getInstance().run {
        timeInMillis = nowTimestamp().ms
        add(Calendar.DATE, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        timeInMillis
    }

}
