package com.simprints.face.controllers.core.crashreport

import com.simprints.id.data.analytics.crashreport.CrashReportTag.*

enum class FaceCrashReportTag {
    FACE_LICENSE,
    FACE_CAPTURE,
    FACE_MATCHING,
    ALERT,
    REFUSAL,
    SAFE_EXCEPTION
}

fun FaceCrashReportTag.fromDomainToCore() =
    when (this) {
        FaceCrashReportTag.FACE_LICENSE -> FACE_LICENSE
        FaceCrashReportTag.FACE_CAPTURE -> FACE_CAPTURE
        FaceCrashReportTag.FACE_MATCHING -> FACE_MATCHING
        FaceCrashReportTag.ALERT -> ALERT
        FaceCrashReportTag.REFUSAL -> REFUSAL
        FaceCrashReportTag.SAFE_EXCEPTION -> SAFE_EXCEPTION
    }
