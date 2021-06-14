package com.simprints.fingerprint.controllers.core.crashreport

import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException
import com.simprints.core.analytics.CoreCrashReportManager

class FingerprintCrashReportManagerImpl(val crashReportManager: CoreCrashReportManager): FingerprintCrashReportManager {

    override fun logExceptionOrSafeException(throwable: Throwable) =
        if(throwable is FingerprintSafeException) {
            crashReportManager.logSafeException(throwable)
        } else {
            crashReportManager.logException(throwable)
        }

    override fun logMessageForCrashReport(crashReportTag: FingerprintCrashReportTag,
                                          crashReportTrigger: FingerprintCrashReportTrigger,
                                          crashPriority: Int,
                                          message: String) =
        crashReportManager.logMessageForCrashReport(
            crashReportTag.fromDomainToCore(),
            crashReportTrigger.fromDomainToCore(),
            crashPriority,
            message)

    override fun logMalfunction(message: String) {
        crashReportManager.logMalfunction(message)
    }

}
