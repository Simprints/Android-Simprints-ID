package com.simprints.fingerprint.controllers.core.crashreport

import com.simprints.core.analytics.CrashReportTrigger.*

enum class FingerprintCrashReportTrigger {
    UI,
    NETWORK,
    SCANNER,
    SCANNER_BUTTON
}

fun FingerprintCrashReportTrigger.fromDomainToCore() =
    when(this) {
        FingerprintCrashReportTrigger.UI -> UI
        FingerprintCrashReportTrigger.NETWORK -> NETWORK
        FingerprintCrashReportTrigger.SCANNER -> SCANNER
        FingerprintCrashReportTrigger.SCANNER_BUTTON -> SCANNER_BUTTON
    }
