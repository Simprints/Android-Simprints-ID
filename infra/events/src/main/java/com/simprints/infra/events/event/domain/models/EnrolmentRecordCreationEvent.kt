package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.utils.EncodingUtils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID
import com.simprints.core.domain.reference.BiometricReference as CoreBiometricReference

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data class")
@Serializable
@SerialName("EnrolmentRecordCreation")
data class EnrolmentRecordCreationEvent(
    override val id: String,
    val payload: EnrolmentRecordCreationPayload,
) : EnrolmentRecordEvent() {
    override val type: EnrolmentRecordEventType
        get() = EnrolmentRecordEventType.EnrolmentRecordCreation

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
    @Serializable
    data class EnrolmentRecordCreationPayload(
        val subjectId: String,
        val projectId: String,
        val moduleId: TokenizableString,
        val attendantId: TokenizableString,
        val biometricReferences: List<BiometricReference> = emptyList(),
        val externalCredentials: List<ExternalCredential> = emptyList(),
    )

    companion object {
        fun buildBiometricReferences(
            references: List<CoreBiometricReference>,
            encoder: EncodingUtils,
        ): List<BiometricReference> = references.mapNotNull { reference ->
            if (reference.templates.isNotEmpty()) {
                when (reference.modality) {
                    Modality.FACE -> buildFaceReference(reference, encoder)
                    Modality.FINGERPRINT -> buildFingerprintReference(reference, encoder)
                }
            } else {
                null
            }
        }

        private fun buildFingerprintReference(
            reference: CoreBiometricReference,
            encoder: EncodingUtils,
        ) = FingerprintReference(
            id = reference.referenceId,
            templates = reference.templates.map {
                FingerprintTemplate(
                    template = encoder.byteArrayToBase64(it.template),
                    finger = it.identifier,
                )
            },
            format = reference.format,
        )

        private fun buildFaceReference(
            reference: CoreBiometricReference,
            encoder: EncodingUtils,
        ) = FaceReference(
            id = reference.referenceId,
            templates = reference.templates.map {
                FaceTemplate(
                    template = encoder.byteArrayToBase64(it.template),
                )
            },
            format = reference.format,
        )
    }
}
