package com.simprints.infra.eventsync.sync.common

import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.reference.BiometricReferenceCapture
import com.simprints.core.domain.reference.BiometricTemplate
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecord
import com.simprints.infra.events.event.domain.models.subject.BiometricReference
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordUpdateEvent.EnrolmentRecordUpdatePayload
import com.simprints.infra.events.event.domain.models.subject.FaceReference
import com.simprints.infra.events.event.domain.models.subject.FingerprintReference
import java.util.Date
import javax.inject.Inject
import com.simprints.core.domain.reference.BiometricReference as CoreBiometricReference

class EnrolmentRecordFactory @Inject constructor(
    private val encodingUtils: EncodingUtils,
    private val timeHelper: TimeHelper,
) {
    fun buildFromCreationPayload(payload: EnrolmentRecordCreationPayload) = with(payload) {
        buildEnrolmentRecord(
            subjectId = subjectId,
            projectId = projectId,
            attendantId = attendantId,
            moduleId = moduleId,
            references = extractFromBiometricReferences(this.biometricReferences),
            externalCredentials = payload.externalCredentials,
        )
    }

    fun buildFromMovePayload(payload: EnrolmentRecordCreationInMove) = with(payload) {
        buildEnrolmentRecord(
            subjectId = subjectId,
            projectId = projectId,
            attendantId = attendantId,
            moduleId = moduleId,
            references = extractFromBiometricReferences(this.biometricReferences),
            externalCredentials = externalCredential?.let { listOf(it) } ?: emptyList(),
        )
    }

    fun buildFromUpdatePayload(
        existingEnrolmentRecord: EnrolmentRecord,
        payload: EnrolmentRecordUpdatePayload,
    ): EnrolmentRecord {
        val removedBiometricReferences = payload.biometricReferencesRemoved.toSet() // to make lookup O(1)
        val addedSamples = extractFromBiometricReferences(payload.biometricReferencesAdded)
        val externalCredentialsAdded = payload.externalCredentialsAdded

        return existingEnrolmentRecord.copy(
            references = existingEnrolmentRecord.references
                .filterNot { it.referenceId in removedBiometricReferences }
                .plus(addedSamples),
            externalCredentials = existingEnrolmentRecord.externalCredentials
                .plus(externalCredentialsAdded)
                .distinctBy { it.value.value },
        )
    }

    fun buildFromCaptureResults(
        subjectId: String,
        projectId: String,
        attendantId: TokenizableString,
        moduleId: TokenizableString,
        captures: List<BiometricReferenceCapture>,
        externalCredential: ExternalCredential?,
    ): EnrolmentRecord = buildEnrolmentRecord(
        subjectId = subjectId,
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        createdAt = Date(timeHelper.now().ms),
        references = captures.map { extractFromCapture(it) },
        externalCredentials = externalCredential?.let { listOf(it) } ?: emptyList(),
    )

    fun buildEnrolmentRecord(
        subjectId: String,
        projectId: String,
        attendantId: TokenizableString,
        moduleId: TokenizableString,
        createdAt: Date? = null,
        updatedAt: Date? = null,
        references: List<CoreBiometricReference> = emptyList(),
        externalCredentials: List<ExternalCredential> = emptyList(),
    ) = EnrolmentRecord(
        subjectId = subjectId,
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        references = references,
        externalCredentials = externalCredentials,
    )

    private fun extractFromCapture(response: BiometricReferenceCapture) = CoreBiometricReference(
        referenceId = response.referenceId,
        format = response.format,
        modality = response.modality,
        templates = response.templates.map {
            BiometricTemplate(
                identifier = it.identifier,
                template = it.template,
            )
        },
    )

    fun extractFromBiometricReferences(biometricReferences: List<BiometricReference>?) = biometricReferences
        ?.map { reference ->
            when (reference) {
                is FingerprintReference -> buildCoreBiometricReference(reference)
                is FaceReference -> buildCoreBiometricReference(reference)
            }
        }
        ?: emptyList()

    private fun buildCoreBiometricReference(reference: FingerprintReference): CoreBiometricReference = CoreBiometricReference(
        referenceId = reference.id,
        format = reference.format,
        modality = Modality.FINGERPRINT,
        templates = reference.templates.map {
            BiometricTemplate(
                identifier = it.finger,
                template = encodingUtils.base64ToBytes(it.template),
            )
        },
    )

    private fun buildCoreBiometricReference(reference: FaceReference) = CoreBiometricReference(
        referenceId = reference.id,
        format = reference.format,
        modality = Modality.FACE,
        templates = reference.templates.map {
            BiometricTemplate(
                template = encodingUtils.base64ToBytes(it.template),
            )
        },
    )
}
