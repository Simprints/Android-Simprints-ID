package com.simprints.face.controllers.core.crashreport

import com.simprints.core.analytics.CrashReportTrigger.NETWORK
import com.simprints.core.analytics.CrashReportTrigger.UI

enum class FaceCrashReportTrigger {
    UI,
    NETWORK
}

fun FaceCrashReportTrigger.fromDomainToCore() =
    when (this) {
        FaceCrashReportTrigger.UI -> UI
        FaceCrashReportTrigger.NETWORK -> NETWORK
    }
