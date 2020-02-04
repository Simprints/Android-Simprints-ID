package com.simprints.id.tools

import android.text.format.DateUtils.*
import org.joda.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit

class TimeHelperImpl : TimeHelper {

    override fun now(): Long = System.currentTimeMillis()

    override fun nowMinus(duration: Long, unit: TimeUnit): Long = now() - unit.toMillis(duration)

    override fun msBetweenNowAndTime(time: Long): Long = now() - time

    override fun readableBetweenNowAndTime(date: Date): String =
        getRelativeTimeSpanString(date.time, Date().time, MINUTE_IN_MILLIS, FORMAT_SHOW_DATE).toString()

    override fun getCurrentDateAsString(): String = LocalDate.now().toString("dd/MM/yyyy")

    override fun todayInMillis(): Long = Calendar.getInstance().run {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        timeInMillis
    }

}
