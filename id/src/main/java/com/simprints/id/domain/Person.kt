package com.simprints.id.domain

import java.util.*
import com.simprints.libcommon.Fingerprint as LibFingerprint
import com.simprints.libcommon.Person as LibPerson

data class Person (
    val patientId: String,
    val projectId: String,
    val userId: String,
    val moduleId: String,
    val createdAt: Date?,
    val updatedAt: Date?,
    val toSync: Boolean,
    val fingerprints: List<Fingerprint>
)

// TODO: move this adapter out of domain code. The domain layer should not be aware of outer layers
fun Person.toLibPerson(): LibPerson =
    LibPerson(
        patientId,
        fingerprints.mapNotNull(Fingerprint::toLibFingerprintOrNull)
    )
