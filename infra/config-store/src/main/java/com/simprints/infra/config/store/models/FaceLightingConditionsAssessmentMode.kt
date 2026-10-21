package com.simprints.infra.config.store.models

enum class FaceLightingConditionsAssessmentMode {
    /** No lighting check. */
    OFF,

    /** Measure lighting and record it in telemetry; show nothing to the user. */
    LOG,

    /** As [LOG], plus a too-dark hint when no face is found on a very dark frame. Never blocks capture. */
    HINT,
    ;

    companion object {
        fun fromString(mode: String): FaceLightingConditionsAssessmentMode = when (mode.uppercase()) {
            "LOG" -> LOG
            "HINT" -> HINT
            else -> OFF
        }
    }
}
