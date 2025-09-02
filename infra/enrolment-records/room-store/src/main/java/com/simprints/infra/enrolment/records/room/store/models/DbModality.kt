package com.simprints.infra.enrolment.records.room.store.models

enum class DbModality(
    val id: Int,
) {
    FINGERPRINT(0),
    FACE(1),
}
