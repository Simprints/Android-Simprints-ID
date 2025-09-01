package com.simprints.infra.eventsync.sync.common

import com.simprints.core.domain.modality.Modality
import com.simprints.core.domain.sample.Sample
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.fingerprint.capture.FingerprintCaptureResult
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
import java.util.UUID
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
        )
    }

    fun buildSubjectFromMovePayload(payload: EnrolmentRecordCreationInMove) = with(payload) {
        buildSubject(
            subjectId = subjectId,
            projectId = projectId,
            attendantId = attendantId,
            moduleId = moduleId,
            samples = extractSamplesFromBiometricReferences(this.biometricReferences),
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
        projectId: String,
        attendantId: TokenizableString,
        moduleId: TokenizableString,
        fingerprintResponse: FingerprintCaptureResult?,
        faceResponse: FaceCaptureResult?,
    ): Subject {
        val subjectId = UUID.randomUUID().toString()
        return buildSubject(
            subjectId = subjectId,
            projectId = projectId,
            attendantId = attendantId,
            moduleId = moduleId,
            createdAt = Date(timeHelper.now().ms),
            samples = fingerprintResponse?.let { extractFingerprintSamples(it) }.orEmpty() +
                faceResponse?.let { extractFaceSamples(it) }.orEmpty(),
        )
    }

    fun buildSubject(
        subjectId: String,
        projectId: String,
        attendantId: TokenizableString,
        moduleId: TokenizableString,
        createdAt: Date? = null,
        updatedAt: Date? = null,
        samples: List<Sample> = emptyList(),
    ) = Subject(
        subjectId = subjectId,
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        samples = samples,
    )

    private fun extractFingerprintSamples(fingerprintResponse: FingerprintCaptureResult) =
        fingerprintResponse.results.mapNotNull { captureResult ->
            captureResult.sample?.let { sample ->
                Sample(
                    identifier = captureResult.identifier,
                    template = sample.template,
                    format = sample.format,
                    referenceId = fingerprintResponse.referenceId,
                    modality = Modality.FINGERPRINT,
                )
            }
        }

    private fun extractFaceSamples(faceResponse: FaceCaptureResult) = faceResponse.results
        .mapNotNull { it.sample }
        .map {
            Sample(
                template = it.template,
                format = it.format,
                referenceId = faceResponse.referenceId,
                modality = Modality.FACE,
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
