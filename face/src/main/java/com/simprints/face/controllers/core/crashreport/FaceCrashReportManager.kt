package com.simprints.face.controllers.core.crashreport

import android.util.Log

interface FaceCrashReportManager {

    fun logMessageForCrashReport(
        crashReportTag: FaceCrashReportTag,
        crashReportTrigger: FaceCrashReportTrigger,
        crashPriority: Int = Log.INFO,
        message: String
    )

    fun logMalfunction(message: String)

}
