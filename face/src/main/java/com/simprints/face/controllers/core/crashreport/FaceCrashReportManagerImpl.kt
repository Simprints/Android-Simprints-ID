package com.simprints.face.controllers.core.crashreport

import com.simprints.core.analytics.CoreCrashReportManager

class FaceCrashReportManagerImpl(private val crashReportManager: CoreCrashReportManager) :
    FaceCrashReportManager {

    override fun logMessageForCrashReport(
        crashReportTag: FaceCrashReportTag,
        crashReportTrigger: FaceCrashReportTrigger,
        crashPriority: Int,
        message: String
    ) =
        crashReportManager.logMessageForCrashReport(
            crashReportTag.fromDomainToCore(),
            crashReportTrigger.fromDomainToCore(),
            crashPriority,
            message
        )

    override fun logMalfunction(message: String) {
        crashReportManager.logMalfunction(message)
    }

    override fun logException(throwable: Throwable) {
        crashReportManager.logException(throwable)
    }
}
