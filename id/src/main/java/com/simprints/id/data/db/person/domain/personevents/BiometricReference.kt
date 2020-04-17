package com.simprints.id.data.db.person.domain.personevents

import com.simprints.id.data.db.person.remote.models.personevents.ApiBiometricReference
import com.simprints.id.data.db.person.remote.models.personevents.ApiFaceReference
import com.simprints.id.data.db.person.remote.models.personevents.ApiFingerprintReference

sealed class BiometricReference(val type: BiometricReferenceType)

class FaceReference(val templates: List<FaceTemplate>,
                    val metadata: HashMap<String, String>? = null): BiometricReference(BiometricReferenceType.FaceReference)

class FingerprintReference(val templates: List<FingerprintTemplate>,
                           val metadata: HashMap<String, String>? = null): BiometricReference(BiometricReferenceType.FingerprintReference)

enum class BiometricReferenceType {
    FaceReference,
    FingerprintReference
}

fun ApiBiometricReference.fromApiToDomain() = when(this) {
    is ApiFaceReference -> this.fromApiToDomain()
    is ApiFingerprintReference -> this.fromApiToDomain()
}

fun ApiFaceReference.fromApiToDomain() = FaceReference(templates.map { it.fromApiToDomain() }, metadata)

fun ApiFingerprintReference.fromApiToDomain() = FingerprintReference(templates.map { it.fromApiToDomain() }, metadata)
