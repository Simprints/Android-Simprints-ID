package com.simprints.infra.enrolment.records.room.store.models

// **IMPORTANT**: Do NOT change the order of this enum as it is used in the database by index.
// Changing the order of the entries in this enum will lead to data corruption or mismatches
// when retrieving or storing data in the database. Always append new entries at the end.
enum class Modality(
    val id: Int,
) {
    FINGERPRINT(0),
    FACE(1),
}
