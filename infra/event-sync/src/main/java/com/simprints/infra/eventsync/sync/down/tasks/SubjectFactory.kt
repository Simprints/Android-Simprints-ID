package com.simprints.infra.eventsync.sync.down.tasks

import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.core.tools.utils.Tokenization
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.enrolment.records.domain.models.Subject
import com.simprints.infra.events.event.domain.models.subject.BiometricReference
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove
import com.simprints.infra.events.event.domain.models.subject.FaceReference
import com.simprints.infra.events.event.domain.models.subject.FaceTemplate
import com.simprints.infra.events.event.domain.models.subject.FingerprintReference
import com.simprints.infra.events.event.domain.models.subject.FingerprintTemplate
import com.simprints.infra.logging.Simber
import java.util.Date
import javax.inject.Inject

class SubjectFactory @Inject constructor(
    private val encodingUtils: EncodingUtils,
    private val tokenization: Tokenization,
    private val configManager: ConfigManager
) {

    suspend fun buildSubjectFromCreationPayload(payload: EnrolmentRecordCreationPayload) =
        with(payload) {
            buildEncryptedSubject(
                subjectId = subjectId,
                projectId = projectId,
                attendantId = attendantId,
                moduleId = moduleId,
                fingerprintSamples = extractFingerprintSamplesFromBiometricReferences(this.biometricReferences),
                faceSamples = extractFaceSamplesFromBiometricReferences(this.biometricReferences)
            )
        }

    suspend fun buildSubjectFromMovePayload(payload: EnrolmentRecordCreationInMove) =
        with(payload) {
            buildEncryptedSubject(
                subjectId = subjectId,
                projectId = projectId,
                attendantId = attendantId,
                moduleId = moduleId,
                fingerprintSamples = extractFingerprintSamplesFromBiometricReferences(this.biometricReferences),
                faceSamples = extractFaceSamplesFromBiometricReferences(this.biometricReferences)
            )
        }

    suspend fun buildEncryptedSubject(
        subjectId: String,
        projectId: String,
        attendantId: String,
        moduleId: String,
        createdAt: Date? = null,
        updatedAt: Date? = null,
        fingerprintSamples: List<FingerprintSample> = emptyList(),
        faceSamples: List<FaceSample> = emptyList()
    ): Subject {
        val project = configManager.getProject(projectId)
        val encryptedAttendantId = encryptToken(
            tokenKeyType = TokenKeyType.AttendantId,
            value = attendantId,
            project = project
        )
        val encryptedModuleId = encryptToken(
            tokenKeyType = TokenKeyType.ModuleId,
            value = moduleId,
            project = project
        )
        return Subject(
            subjectId = subjectId,
            projectId = projectId,
            attendantId = encryptedAttendantId,
            moduleId = encryptedModuleId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            fingerprintSamples = fingerprintSamples,
            faceSamples = faceSamples
        )
    }

    private fun encryptToken(tokenKeyType: TokenKeyType, value: String, project: Project): String =
        try {
            project.tokenizationKeys[tokenKeyType]?.let { json ->
                tokenization.encrypt(value = value, keysetJson = json)
            } ?: value
        } catch (e: Exception) {
            Simber.e("Unable to tokenize [$tokenKeyType]: $e")
            value
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
        format: String
    ): FingerprintSample {
        return FingerprintSample(
            template.finger,
            encodingUtils.base64ToBytes(template.template),
            template.quality,
            format
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
