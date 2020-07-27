package com.simprints.id.data.db.event.domain.models.subject

import com.beust.klaxon.TypeAdapter
import com.simprints.id.data.db.event.domain.models.subject.BiometricReferenceType.FACE_REFERENCE
import com.simprints.id.data.db.event.domain.models.subject.BiometricReferenceType.FINGERPRINT_REFERENCE
import kotlin.reflect.KClass

class BiometricReferenceTypeAdapter : TypeAdapter<BiometricReference> {
    override fun classFor(type: Any): KClass<out BiometricReference> {
        return when (BiometricReferenceType.valueOf(type as String)) {
            FACE_REFERENCE -> FaceReference::class
            FINGERPRINT_REFERENCE -> FingerprintReference::class
        }
    }
}
