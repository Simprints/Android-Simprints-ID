package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.modality.Modality
import com.simprints.infra.enrolment.records.repository.local.models.DbModality.FACE
import com.simprints.infra.enrolment.records.repository.local.models.DbModality.FINGERPRINT

enum class DbModality(
    val id: Int,
) {
    FINGERPRINT(0),
    FACE(1),
    ;

    companion object {
        fun fromId(id: Int?) = DbModality.entries
            .firstOrNull { it.id == id }
            ?: throw IllegalArgumentException("Invalid id: $id")
    }
}

internal fun DbModality.toDomain(): Modality = when (this) {
    FINGERPRINT -> Modality.FINGERPRINT
    FACE -> Modality.FACE
}

internal fun Modality.fromDomain(): DbModality = when (this) {
    Modality.FINGERPRINT -> FINGERPRINT
    Modality.FACE -> FACE
}
