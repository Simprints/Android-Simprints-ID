package com.simprints.infra.eventsync.sync.down.tasks

import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
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
            fingerprintSamples = extractFingerprintSamplesFromBiometricReferences(this.biometricReferences),
            faceSamples = extractFaceSamplesFromBiometricReferences(this.biometricReferences),
        )
    }

    fun buildSubjectFromMovePayload(payload: EnrolmentRecordCreationInMove) = with(payload) {
        buildSubject(
            subjectId = subjectId,
            projectId = projectId,
            attendantId = attendantId,
            moduleId = moduleId,
            fingerprintSamples = extractFingerprintSamplesFromBiometricReferences(this.biometricReferences),
            faceSamples = extractFaceSamplesFromBiometricReferences(this.biometricReferences),
        )
    }

    fun buildSubjectFromUpdatePayload(
        existingSubject: Subject,
        payload: EnrolmentRecordUpdatePayload,
    ): Subject {
        val removedBiometricReferences = payload.biometricReferencesRemoved.toSet() // to make lookup O(1)
        val addedFaceSamples = extractFaceSamplesFromBiometricReferences(payload.biometricReferencesAdded)
        val addedFingerprintSamples = extractFingerprintSamplesFromBiometricReferences(payload.biometricReferencesAdded)

        return existingSubject.copy(
            faceSamples = existingSubject.faceSamples
                .filterNot { it.referenceId in removedBiometricReferences }
                .plus(addedFaceSamples),
            fingerprintSamples = existingSubject.fingerprintSamples
                .filterNot { it.referenceId in removedBiometricReferences }
                .plus(addedFingerprintSamples),
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
            fingerprintSamples = fingerprintResponse?.let { extractFingerprintSamples(it) }.orEmpty(),
            faceSamples = faceResponse?.let { extractFaceSamples(it) }.orEmpty(),
        )
    }

    fun buildSubject(
        subjectId: String,
        projectId: String,
        attendantId: TokenizableString,
        moduleId: TokenizableString,
        createdAt: Date? = null,
        updatedAt: Date? = null,
        fingerprintSamples: List<FingerprintSample> = emptyList(),
        faceSamples: List<FaceSample> = emptyList(),
    ) = Subject(
        subjectId = subjectId,
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        fingerprintSamples = fingerprintSamples,
        faceSamples = faceSamples,
    )

    private fun extractFingerprintSamples(fingerprintResponse: FingerprintCaptureResult) =
        fingerprintResponse.results.mapNotNull { captureResult ->
            captureResult.sample?.let { sample ->
                FingerprintSample(
                    captureResult.identifier,
                    sample.template,
                    sample.templateQualityScore,
                    sample.format,
                    fingerprintResponse.referenceId,
                )
            }
        }

    private fun extractFaceSamples(faceResponse: FaceCaptureResult) = faceResponse.results
        .mapNotNull { it.sample }
        .map { FaceSample(it.template, it.format, faceResponse.referenceId) }

    fun extractFingerprintSamplesFromBiometricReferences(biometricReferences: List<BiometricReference>?) = biometricReferences
        ?.filterIsInstance<FingerprintReference>()
        ?.map { reference -> reference.templates.map { buildFingerprintSample(it, reference.format, reference.id) } }
        ?.flatten()
        ?: emptyList()

    private fun buildFingerprintSample(
        template: FingerprintTemplate,
        format: String,
        referenceId: String,
    ): FingerprintSample = FingerprintSample(
        fingerIdentifier = template.finger,
        template = encodingUtils.base64ToBytes(template.template),
        templateQualityScore = template.quality,
        format = format,
        referenceId = referenceId,
    )

    fun extractFaceSamplesFromBiometricReferences(biometricReferences: List<BiometricReference>?) = biometricReferences
        ?.filterIsInstance<FaceReference>()
        ?.map { reference -> reference.templates.map { buildFaceSample(it, reference.format, reference.id) } }
        ?.flatten()
        ?: emptyList()

    private fun buildFaceSample(
        template: FaceTemplate,
        format: String,
        referenceId: String,
    ) = FaceSample(
        template = encodingUtils.base64ToBytes(template.template),
        format = format,
        referenceId = referenceId,
    )
}
