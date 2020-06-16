package com.simprints.face.controllers.core.crashreport

import com.simprints.id.data.analytics.crashreport.CrashReportTrigger.NETWORK
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger.UI

enum class FaceCrashReportTrigger {
    UI,
    NETWORK
}

fun FaceCrashReportTrigger.fromDomainToCore() =
    when (this) {
        FaceCrashReportTrigger.UI -> UI
        FaceCrashReportTrigger.NETWORK -> NETWORK
    }
