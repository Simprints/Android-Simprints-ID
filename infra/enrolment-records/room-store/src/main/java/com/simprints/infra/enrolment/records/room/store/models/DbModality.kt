package com.simprints.infra.enrolment.records.room.store.models

import com.simprints.core.domain.common.Modality

enum class DbModality(
    val id: Int,
) {
    FINGERPRINT(0),
    FACE(1),
    ;

    companion object Companion {
        fun fromId(id: Int) = DbModality.entries.firstOrNull { it.id == id }
            ?: throw IllegalArgumentException("Unknown sample identifier id: $id")
    }
}

fun DbModality.toDomain() = when (this) {
    DbModality.FINGERPRINT -> Modality.FINGERPRINT
    DbModality.FACE -> Modality.FACE
}

fun Modality.fromDomain() = when (this) {
    Modality.FINGERPRINT -> DbModality.FINGERPRINT
    Modality.FACE -> DbModality.FACE
}
