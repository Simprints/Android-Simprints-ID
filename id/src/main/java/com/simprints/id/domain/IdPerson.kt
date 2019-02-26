package com.simprints.id.domain

import com.simprints.id.domain.fingerprint.IdFingerprint
import com.simprints.id.domain.fingerprint.Person
import com.simprints.id.domain.fingerprint.toLibFingerprintOrNull
import java.util.*

data class IdPerson (
    val patientId: String,
    val projectId: String,
    val userId: String,
    val moduleId: String,
    val createdAt: Date?,
    val updatedAt: Date?,
    val toSync: Boolean, // TODO: stop leaking data layer concerns into domain layer
    val idFingerprints: List<IdFingerprint>
)

// TODO: move this adapter out of domain code. The domain layer should not be aware of outer layers
fun IdPerson.toLibPerson(): Person =
    Person(
        patientId,
        idFingerprints.mapNotNull(IdFingerprint::toLibFingerprintOrNull)
    )
