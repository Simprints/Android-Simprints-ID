package com.simprints.infra.eventsync.sync.down.tasks

import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.enrolment.records.domain.models.Subject
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.infra.events.event.domain.models.subject.BiometricReference
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove
import com.simprints.infra.events.event.domain.models.subject.FaceReference
import com.simprints.infra.events.event.domain.models.subject.FaceTemplate
import com.simprints.infra.events.event.domain.models.subject.FingerprintReference
import com.simprints.infra.events.event.domain.models.subject.FingerprintTemplate
import javax.inject.Inject

internal class SubjectFactory @Inject constructor(private val encodingUtils: EncodingUtils) {

    fun buildSubjectFromCreationPayload(payload: EnrolmentRecordCreationPayload) =
        with(payload) {
            Subject(
                subjectId = subjectId,
                projectId = projectId,
                attendantId = attendantId,
                moduleId = moduleId,
                fingerprintSamples = extractFingerprintSamplesFromBiometricReferences(this.biometricReferences),
                faceSamples = extractFaceSamplesFromBiometricReferences(this.biometricReferences)
            )
        }

    fun buildSubjectFromMovePayload(payload: EnrolmentRecordCreationInMove) =
        with(payload) {
            Subject(
                subjectId = subjectId,
                projectId = projectId,
                attendantId = attendantId,
                moduleId = moduleId,
                fingerprintSamples = extractFingerprintSamplesFromBiometricReferences(this.biometricReferences),
                faceSamples = extractFaceSamplesFromBiometricReferences(this.biometricReferences)
            )
        }

    private fun extractFingerprintSamplesFromBiometricReferences(biometricReferences: List<BiometricReference>?) =
        biometricReferences?.filterIsInstance<FingerprintReference>()
            ?.firstOrNull()
            ?.let { reference ->
                reference.templates.map {
                    buildFingerprintSample(
                        it,
                        reference.format
                    )
                }
            }
            ?: emptyList()

    private fun buildFingerprintSample(
        template: FingerprintTemplate,
        format: FingerprintTemplateFormat
    ): FingerprintSample {
        return FingerprintSample(
            template.finger,
            encodingUtils.base64ToBytes(template.template),
            template.quality,
            format.fromDomainToModuleApi()
        )
    }

    private fun extractFaceSamplesFromBiometricReferences(biometricReferences: List<BiometricReference>?) =
        biometricReferences?.filterIsInstance<FaceReference>()
            ?.firstOrNull()
            ?.let { reference -> reference.templates.map { buildFaceSample(it, reference.format) } }
            ?: emptyList()

    private fun buildFaceSample(template: FaceTemplate, format: String) =
        FaceSample(encodingUtils.base64ToBytes(template.template), format)
}
