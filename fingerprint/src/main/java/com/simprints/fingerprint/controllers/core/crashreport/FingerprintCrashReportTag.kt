package com.simprints.fingerprint.controllers.core.crashreport
import com.simprints.id.data.analytics.crashreport.CrashReportTag.*

enum class FingerprintCrashReportTag {
    SCANNER_SETUP,
    FINGER_CAPTURE,
    MATCHING,
    LONG_CONSENT,
    ALERT,
    REFUSAL,
    SAFE_EXCEPTION
}

fun FingerprintCrashReportTag.fromDomainToCore() =
    when(this) {
        FingerprintCrashReportTag.SCANNER_SETUP -> SCANNER_SETUP
        FingerprintCrashReportTag.FINGER_CAPTURE -> FINGER_CAPTURE
        FingerprintCrashReportTag.MATCHING -> MATCHING
        FingerprintCrashReportTag.LONG_CONSENT -> LONG_CONSENT
        FingerprintCrashReportTag.ALERT -> ALERT
        FingerprintCrashReportTag.REFUSAL -> REFUSAL
        FingerprintCrashReportTag.SAFE_EXCEPTION -> SAFE_EXCEPTION
    }
