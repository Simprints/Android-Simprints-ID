package com.simprints.id.data.db.person.domain.personevents

import com.simprints.id.data.db.person.remote.models.personevents.ApiBiometricReference
import com.simprints.id.data.db.person.remote.models.personevents.ApiFaceReference
import com.simprints.id.data.db.person.remote.models.personevents.ApiFingerprintReference

sealed class BiometricReference(val type: BiometricReferenceType)

class FaceReference(val metadata: HashMap<String, String>,
                    val templates: List<FaceTemplate>): BiometricReference(BiometricReferenceType.FaceReference)

class FingerprintReference(val metadata: HashMap<String, String>,
                           val templates: List<FingerprintTemplate>): BiometricReference(BiometricReferenceType.FingerprintReference)

enum class BiometricReferenceType {
    FaceReference,
    FingerprintReference
}

fun ApiBiometricReference.fromApiToDomain() = when(this) {
    is ApiFaceReference -> this.fromApiToDomain()
    is ApiFingerprintReference -> this.fromApiToDomain()
}

fun ApiFaceReference.fromApiToDomain() = FaceReference(metadata, templates.map { it.fromApiToDomain() })

fun ApiFingerprintReference.fromApiToDomain() = FingerprintReference(metadata, templates.map { it.fromApiToDomain() })
