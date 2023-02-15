package com.simprints.eventsystem.event.domain.models.subject

import androidx.annotation.Keep
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.face.uniqueId
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.uniqueId
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.eventsystem.event.domain.models.face.fromModuleApiToDomain
import com.simprints.eventsystem.event.domain.models.fingerprint.fromModuleApiToDomain
import java.util.*

@Keep
data class EnrolmentRecordCreationEvent(
    override val id: String,
    val payload: EnrolmentRecordCreationPayload,
) : EnrolmentRecordEvent(id, EnrolmentRecordEventType.EnrolmentRecordCreation) {

    constructor(
        subjectId: String,
        projectId: String,
        moduleId: String,
        attendantId: String,
        biometricReferences: List<BiometricReference>,
    ) : this(
        UUID.randomUUID().toString(),
        EnrolmentRecordCreationPayload(
            subjectId,
            projectId,
            moduleId,
            attendantId,
            biometricReferences
        )
    )

    data class EnrolmentRecordCreationPayload(
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String,
        val biometricReferences: List<BiometricReference>,
    )

    companion object {

        fun buildBiometricReferences(
            fingerprintSamples: List<FingerprintSample>,
            faceSamples: List<FaceSample>,
            encoder: EncodingUtils
        ): List<BiometricReference> {
            val biometricReferences = mutableListOf<BiometricReference>()

            buildFingerprintReference(fingerprintSamples, encoder)?.let {
                biometricReferences.add(it)
            }

            buildFaceReference(faceSamples, encoder)?.let {
                biometricReferences.add(it)
            }

            return biometricReferences
        }

        private fun buildFingerprintReference(
            fingerprintSamples: List<FingerprintSample>,
            encoder: EncodingUtils
        ) =
            if (fingerprintSamples.isNotEmpty()) {
                FingerprintReference(
                    fingerprintSamples.uniqueId() ?: "",
                    fingerprintSamples.map {
                        FingerprintTemplate(
                            it.templateQualityScore,
                            encoder.byteArrayToBase64(it.template),
                            it.fingerIdentifier
                        )
                    },
                    fingerprintSamples.first().format.fromModuleApiToDomain()
                )
            } else {
                null
            }

        private fun buildFaceReference(
            faceSamples: List<FaceSample>,
            encoder: EncodingUtils
        ) =
            if (faceSamples.isNotEmpty()) {
                FaceReference(
                    faceSamples.uniqueId() ?: "",
                    faceSamples.map {
                        FaceTemplate(
                            encoder.byteArrayToBase64(it.template)
                        )
                    },
                    faceSamples.first().format.fromModuleApiToDomain()
                )
            } else {
                null
            }
    }
}
