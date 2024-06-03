package com.simprints.infra.enrolment.records.store.domain.models

import com.simprints.infra.enrolment.records.store.commcare.CommCareIdentityDataSource

enum class BiometricDataSource {
    SIMPRINTS,
    COMMCARE;

    companion object {

        fun fromString(value: String) = when (value.uppercase()) {
            "COMMCARE" -> COMMCARE
            else -> SIMPRINTS
        }

        fun BiometricDataSource.permissionName(): String? = when (this) {
            COMMCARE -> CommCareIdentityDataSource.PERMISSION_NAME
            else -> null
        }
    }
}
