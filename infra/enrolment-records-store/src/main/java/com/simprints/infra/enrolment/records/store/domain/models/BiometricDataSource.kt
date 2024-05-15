package com.simprints.infra.enrolment.records.store.domain.models

enum class BiometricDataSource {
    SIMPRINTS,
    COMMCARE;

    companion object {
        fun fromString(value: String) = when (value.uppercase()) {
            "COMMCARE" -> COMMCARE
            else -> SIMPRINTS
        }
    }
}