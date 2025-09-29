package com.simprints.infra.events.event.domain.models.subject

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.sample.Sample
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.utils.EncodingUtils
import java.util.UUID

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data class")
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
        externalCredentials: List<ExternalCredential>,
    ) : this(
        UUID.randomUUID().toString(),
        EnrolmentRecordCreationPayload(
            subjectId = subjectId,
            projectId = projectId,
            moduleId = moduleId,
            attendantId = attendantId,
            biometricReferences = biometricReferences,
            externalCredentials = externalCredentials,
        ),
    )

    @Keep
    data class EnrolmentRecordCreationPayload(
        val subjectId: String,
        val projectId: String,
        val moduleId: TokenizableString,
        val attendantId: TokenizableString,
        val biometricReferences: List<BiometricReference>,
        val externalCredentials: List<ExternalCredential>,
    )

    companion object {
        fun buildBiometricReferences(
            samples: List<Sample>,
            encoder: EncodingUtils,
        ): List<BiometricReference> = samples.groupBy { it.modality }.mapNotNull { (modality, modalitySamples) ->
            if (modalitySamples.isNotEmpty()) {
                when (modality) {
                    Modality.FACE -> buildFingerprintReference(modalitySamples, encoder)
                    Modality.FINGERPRINT -> buildFaceReference(modalitySamples, encoder)
                }
            } else {
                null
            }
        }

        private fun buildFingerprintReference(
            fingerprintSamples: List<Sample>,
            encoder: EncodingUtils,
        ) = FingerprintReference(
            fingerprintSamples.first().referenceId,
            fingerprintSamples.map {
                FingerprintTemplate(
                    encoder.byteArrayToBase64(it.template),
                    it.identifier,
                )
            },
            fingerprintSamples.first().format,
        )

        private fun buildFaceReference(
            faceSamples: List<Sample>,
            encoder: EncodingUtils,
        ) = FaceReference(
            faceSamples.first().referenceId,
            faceSamples.map {
                FaceTemplate(
                    encoder.byteArrayToBase64(it.template),
                )
            },
            faceSamples.first().format,
        )
    }
}
