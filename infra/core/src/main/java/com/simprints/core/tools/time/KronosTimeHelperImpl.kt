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

class KronosTimeHelperImpl @Inject constructor(
    private val clock: KronosClock,
) : TimeHelper {
    init {
        clock.syncInBackground()
    }

    override fun ensureTrustworthiness() {
        clock.sync()
    }

    override fun now(): Timestamp = clock.getCurrentTime().let {
        Timestamp(
            ms = it.posixTimeMs,
            isTrustworthy = it.timeSinceLastNtpSyncMs != null,
            msSinceBoot = clock.getElapsedTimeMs(),
        )
    }

    override fun msBetweenNowAndTime(time: Timestamp): Long = now().ms - time.ms

    override fun readableBetweenNowAndTime(date: Timestamp): String =
        getRelativeTimeSpanString(date.ms, now().ms, MINUTE_IN_MILLIS, FORMAT_SHOW_DATE).toString()

    override fun getCurrentDateAsString(): String {
        val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
        return dateFormat.format(Date(now().ms))
    }

    override fun todayInMillis(): Long = Calendar.getInstance().run {
        timeInMillis = now().ms
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        timeInMillis
    }

    override fun tomorrowInMillis(): Long = Calendar.getInstance().run {
        timeInMillis = now().ms
        add(Calendar.DATE, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        timeInMillis
    }
}
