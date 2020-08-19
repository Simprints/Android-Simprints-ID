package com.simprints.id.tools.time

import android.text.format.DateUtils.*
import com.lyft.kronos.KronosClock
import com.simprints.id.tools.TimeHelper
import java.text.DateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class KronosTimeHelperImpl(private val clock: KronosClock) : TimeHelper {

    init {
        clock.syncInBackground()
    }

    override fun now(): Long = clock.getCurrentTimeMs()

    override fun nowMinus(duration: Long, unit: TimeUnit): Long = now() - unit.toMillis(duration)

    override fun msBetweenNowAndTime(time: Long): Long = now() - time

    override fun readableBetweenNowAndTime(date: Date): String =
        getRelativeTimeSpanString(date.time, Date().time, MINUTE_IN_MILLIS, FORMAT_SHOW_DATE).toString()

    override fun getCurrentDateAsString(): String {
        val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
        return dateFormat.format(Date())
    }

    override fun todayInMillis(): Long = Calendar.getInstance().run {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        timeInMillis
    }

    override fun tomorrowInMillis(): Long = Calendar.getInstance().run {
        add(Calendar.DATE, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        timeInMillis
    }

}
