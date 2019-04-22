package com.simprints.fingerprint.controllers.core.crashreport

import android.util.Log
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger

interface FingerprintCrashReportManager {

    fun logMessageForCrashReport(crashReportTag: CrashReportTag,
                                 crashReportTrigger: CrashReportTrigger,
                                 crashPriority: Int = Log.INFO,
                                 message: String)

    fun logExceptionOrThrowable(throwable: Throwable)

}
