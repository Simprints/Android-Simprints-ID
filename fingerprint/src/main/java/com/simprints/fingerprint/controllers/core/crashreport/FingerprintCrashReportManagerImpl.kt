package com.simprints.fingerprint.controllers.core.crashreport

import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger

class FingerprintCrashReportManagerImpl(val crashReportManager: CrashReportManager): FingerprintCrashReportManager {
    override fun logExceptionOrThrowable(t: Throwable) =
        crashReportManager.logExceptionOrThrowable(t)

    override fun logMessageForCrashReport(matching: CrashReportTag, ui: CrashReportTrigger, info: Int, message: String) =
        crashReportManager.logMessageForCrashReport(matching, ui, info, message)
}
