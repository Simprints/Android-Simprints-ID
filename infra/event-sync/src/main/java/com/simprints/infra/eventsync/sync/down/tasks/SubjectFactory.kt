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
                )
            }
        }

    private fun extractFaceSamples(faceResponse: FaceCaptureResult) = faceResponse.results
        .mapNotNull { it.sample }
        .map { FaceSample(it.template, it.format) }

    private fun extractFingerprintSamplesFromBiometricReferences(biometricReferences: List<BiometricReference>?) = biometricReferences
        ?.filterIsInstance<FingerprintReference>()
        ?.firstOrNull()
        ?.let { reference ->
            reference.templates.map {
                buildFingerprintSample(
                    it,
                    reference.format,
                )
            }
        }
        ?: emptyList()

    private fun buildFingerprintSample(
        template: FingerprintTemplate,
        format: String,
    ): FingerprintSample = FingerprintSample(
        fingerIdentifier = template.finger,
        template = encodingUtils.base64ToBytes(template.template),
        templateQualityScore = template.quality,
        format = format,
    )

    private fun extractFaceSamplesFromBiometricReferences(biometricReferences: List<BiometricReference>?) = biometricReferences
        ?.filterIsInstance<FaceReference>()
        ?.firstOrNull()
        ?.let { reference -> reference.templates.map { buildFaceSample(it, reference.format) } }
        ?: emptyList()

    private fun buildFaceSample(
        template: FaceTemplate,
        format: String,
    ) = FaceSample(encodingUtils.base64ToBytes(template.template), format)
}
