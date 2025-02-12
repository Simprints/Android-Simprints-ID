package com.simprints.infra.events.event.domain.models.subject

import androidx.annotation.Keep
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.utils.EncodingUtils
import java.util.UUID

@Keep
data class EnrolmentRecordCreationEvent(
    override val id: String,
    val payload: EnrolmentRecordCreationPayload,
) : EnrolmentRecordEvent(id, EnrolmentRecordEventType.EnrolmentRecordCreation) {
    constructor(
        subjectId: String,
        projectId: String,
        moduleId: TokenizableString,
        attendantId: TokenizableString,
        biometricReferences: List<BiometricReference>,
    ) : this(
        UUID.randomUUID().toString(),
        EnrolmentRecordCreationPayload(
            subjectId,
            projectId,
            moduleId,
            attendantId,
            biometricReferences,
        ),
    )

    @Keep
    data class EnrolmentRecordCreationPayload(
        val subjectId: String,
        val projectId: String,
        val moduleId: TokenizableString,
        val attendantId: TokenizableString,
        val biometricReferences: List<BiometricReference>,
    )

    companion object {
        fun buildBiometricReferences(
            fingerprintSamples: List<FingerprintSample>,
            faceSamples: List<FaceSample>,
            encoder: EncodingUtils,
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
            encoder: EncodingUtils,
        ) = if (fingerprintSamples.isNotEmpty()) {
            FingerprintReference(
                fingerprintSamples.first().referenceId,
                fingerprintSamples.map {
                    FingerprintTemplate(
                        it.templateQualityScore,
                        encoder.byteArrayToBase64(it.template),
                        it.fingerIdentifier,
                    )
                },
                fingerprintSamples.first().format,
            )
        } else {
            null
        }

        private fun buildFaceReference(
            faceSamples: List<FaceSample>,
            encoder: EncodingUtils,
        ) = if (faceSamples.isNotEmpty()) {
            FaceReference(
                faceSamples.first().referenceId,
                faceSamples.map {
                    FaceTemplate(
                        encoder.byteArrayToBase64(it.template),
                    )
                },
                faceSamples.first().format,
            )
        } else {
            null
        }
    }
}
