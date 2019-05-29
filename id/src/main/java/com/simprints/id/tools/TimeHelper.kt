package com.simprints.id.tools

import com.simprints.id.Application
import com.simprints.id.data.analytics.crashreport.CoreCrashReportManager
import java.util.concurrent.TimeUnit

interface TimeHelper {

    fun now(): Long
    fun nowMinus(duration: Long, unit: TimeUnit = TimeUnit.MILLISECONDS): Long
    fun msBetweenNowAndTime(time: Long): Long

    companion object {
        fun build(app: Application): TimeHelper =
            app.component.getTimeHelper()
    }
}
