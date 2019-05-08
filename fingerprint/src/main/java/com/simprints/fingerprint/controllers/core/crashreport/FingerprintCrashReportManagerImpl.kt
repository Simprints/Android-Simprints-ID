package com.simprints.fingerprint.controllers.core.crashreport

import com.simprints.id.data.analytics.crashreport.CrashReportManager

class FingerprintCrashReportManagerImpl(val crashReportManager: CrashReportManager): FingerprintCrashReportManager {

    override fun logExceptionOrThrowable(throwable: Throwable) =
        crashReportManager.logExceptionOrThrowable(throwable)

    override fun logMessageForCrashReport(crashReportTag: FingerprintCrashReportTag,
                                          crashReportTrigger: FingerprintCrashReportTrigger,
                                          crashPriority: Int,
                                          message: String) =
        crashReportManager.logMessageForCrashReport(
            crashReportTag.fromDomainToCore(),
            crashReportTrigger.fromDomainToCore(),
            crashPriority,
            message)
}
