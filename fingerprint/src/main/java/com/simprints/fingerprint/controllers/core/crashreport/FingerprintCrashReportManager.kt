package com.simprints.fingerprint.controllers.core.crashreport

import android.util.Log

interface FingerprintCrashReportManager {

    fun logMessageForCrashReport(crashReportTag: FingerprintCrashReportTag,
                                 crashReportTrigger: FingerprintCrashReportTrigger,
                                 crashPriority: Int = Log.INFO,
                                 message: String)

    fun logExceptionOrSafeException(throwable: Throwable)

}
