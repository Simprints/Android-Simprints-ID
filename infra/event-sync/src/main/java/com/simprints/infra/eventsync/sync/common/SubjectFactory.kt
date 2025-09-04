package com.simprints.infra.eventsync.sync.common

import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.externalcredential.ExternalCredential
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
            externalCredentials = payload.externalCredentials,
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
            externalCredentials = externalCredential?.let { listOf(it) } ?: emptyList(),
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
        subjectId: String,
        projectId: String,
        attendantId: TokenizableString,
        moduleId: TokenizableString,
        fingerprintResponse: FingerprintCaptureResult?,
        faceResponse: FaceCaptureResult?,
        externalCredential: ExternalCredential?,
    ): Subject = buildSubject(
        subjectId = subjectId,
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        createdAt = Date(timeHelper.now().ms),
        fingerprintSamples = fingerprintResponse?.let { extractFingerprintSamples(it) }.orEmpty(),
        faceSamples = faceResponse?.let { extractFaceSamples(it) }.orEmpty(),
        externalCredentials = externalCredential?.let { listOf(it) } ?: emptyList(),
    )

    fun buildSubject(
        subjectId: String,
        projectId: String,
        attendantId: TokenizableString,
        moduleId: TokenizableString,
        createdAt: Date? = null,
        updatedAt: Date? = null,
        fingerprintSamples: List<Sample> = emptyList(),
        faceSamples: List<Sample> = emptyList(),
        externalCredentials: List<ExternalCredential> = emptyList(),
    ) = Subject(
        subjectId = subjectId,
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        fingerprintSamples = fingerprintSamples,
        faceSamples = faceSamples,
        externalCredentials = externalCredentials,
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

    fun extractFingerprintSamplesFromBiometricReferences(biometricReferences: List<BiometricReference>?) = biometricReferences
        ?.filterIsInstance<FingerprintReference>()
        ?.map { reference -> reference.templates.map { buildFingerprintSample(it, reference.format, reference.id) } }
        ?.flatten()
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

    fun extractFaceSamplesFromBiometricReferences(biometricReferences: List<BiometricReference>?) = biometricReferences
        ?.filterIsInstance<FaceReference>()
        ?.map { reference -> reference.templates.map { buildFaceSample(it, reference.format, reference.id) } }
        ?.flatten()
        ?: emptyList()

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
