package com.simprints.infra.eventsync.sync.common

import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.sample.CaptureIdentity
import com.simprints.core.domain.sample.Sample
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.events.event.domain.models.subject.BiometricReference
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordUpdateEvent.EnrolmentRecordUpdatePayload
import com.simprints.infra.events.event.domain.models.subject.FaceReference
import com.simprints.infra.events.event.domain.models.subject.FaceTemplate
import com.simprints.infra.events.event.domain.models.subject.FingerprintReference
import com.simprints.infra.events.event.domain.models.subject.FingerprintTemplate
import java.util.Date
import javax.inject.Inject

class SubjectFactory @Inject constructor(
    private val encodingUtils: EncodingUtils,
    private val timeHelper: TimeHelper,
) {
    fun buildSubjectFromCreationPayload(payload: EnrolmentRecordCreationPayload) = with(payload) {
        buildSubject(
            subjectId = subjectId,
            projectId = projectId,
            attendantId = attendantId,
            moduleId = moduleId,
            samples = extractSamplesFromBiometricReferences(this.biometricReferences),
            externalCredentials = payload.externalCredentials,
        )
    }

    fun buildSubjectFromMovePayload(payload: EnrolmentRecordCreationInMove) = with(payload) {
        buildSubject(
            subjectId = subjectId,
            projectId = projectId,
            attendantId = attendantId,
            moduleId = moduleId,
            samples = extractSamplesFromBiometricReferences(this.biometricReferences),
            externalCredentials = externalCredential?.let { listOf(it) } ?: emptyList(),
        )
    }

    fun buildSubjectFromUpdatePayload(
        existingSubject: Subject,
        payload: EnrolmentRecordUpdatePayload,
    ): Subject {
        val removedBiometricReferences = payload.biometricReferencesRemoved.toSet() // to make lookup O(1)
        val addedSamples = extractSamplesFromBiometricReferences(payload.biometricReferencesAdded)

        return existingSubject.copy(
            samples = existingSubject.samples
                .filterNot { it.referenceId in removedBiometricReferences }
                .plus(addedSamples),
        )
    }

    fun buildSubjectFromCaptureResults(
        subjectId: String,
        projectId: String,
        attendantId: TokenizableString,
        moduleId: TokenizableString,
        captures: List<CaptureIdentity>,
        externalCredential: ExternalCredential?,
    ): Subject = buildSubject(
        subjectId = subjectId,
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        createdAt = Date(timeHelper.now().ms),
        samples = captures.flatMap { extractCaptureSamples(it) },
        externalCredentials = externalCredential?.let { listOf(it) } ?: emptyList(),
    )

    fun buildSubject(
        subjectId: String,
        projectId: String,
        attendantId: TokenizableString,
        moduleId: TokenizableString,
        createdAt: Date? = null,
        updatedAt: Date? = null,
        samples: List<Sample> = emptyList(),
        externalCredentials: List<ExternalCredential> = emptyList(),
    ) = Subject(
        subjectId = subjectId,
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        samples = samples,
        externalCredentials = externalCredentials,
    )

    private fun extractCaptureSamples(response: CaptureIdentity) = response.results.map { sample ->
        Sample(
            identifier = sample.identifier,
            template = sample.template,
            format = sample.format,
            referenceId = response.referenceId,
            modality = sample.modality,
        )
    }

    fun extractSamplesFromBiometricReferences(biometricReferences: List<BiometricReference>?) = biometricReferences
        ?.map { reference ->
            when (reference) {
                is FingerprintReference -> reference.templates.map { buildFingerprintSample(it, reference.format, reference.id) }
                is FaceReference -> reference.templates.map { buildFaceSample(it, reference.format, reference.id) }
            }
        }?.flatten()
        ?: emptyList()

    private fun buildFingerprintSample(
        template: FingerprintTemplate,
        format: String,
        referenceId: String,
    ): Sample = Sample(
        identifier = template.finger,
        template = encodingUtils.base64ToBytes(template.template),
        format = format,
        referenceId = referenceId,
        modality = Modality.FINGERPRINT,
    )

    private fun buildFaceSample(
        template: FaceTemplate,
        format: String,
        referenceId: String,
    ) = Sample(
        template = encodingUtils.base64ToBytes(template.template),
        format = format,
        referenceId = referenceId,
        modality = Modality.FACE,
    )
}
