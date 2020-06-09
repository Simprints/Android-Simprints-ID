package com.simprints.face.controllers.core.crashreport

import com.simprints.id.data.analytics.crashreport.CrashReportTag.*

enum class FaceCrashReportTag {
    SCANNER_SETUP,
    FINGER_CAPTURE,
    MATCHING,
    LONG_CONSENT,
    ALERT,
    REFUSAL,
    SAFE_EXCEPTION
}

fun FaceCrashReportTag.fromDomainToCore() =
    when (this) {
        FaceCrashReportTag.SCANNER_SETUP -> SCANNER_SETUP
        FaceCrashReportTag.FINGER_CAPTURE -> FINGER_CAPTURE
        FaceCrashReportTag.MATCHING -> MATCHING
        FaceCrashReportTag.LONG_CONSENT -> LONG_CONSENT
        FaceCrashReportTag.ALERT -> ALERT
        FaceCrashReportTag.REFUSAL -> REFUSAL
        FaceCrashReportTag.SAFE_EXCEPTION -> SAFE_EXCEPTION
    }
