package com.simprints.id.commontesttools

import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.db.person.domain.personevents.*
import com.simprints.id.domain.modality.Modes
import java.util.*

object EnrolmentRecordsGeneratorUtils {
    fun getRandomEnrolmentEvents(nEvents: Int,
                                 projectId: String,
                                 userId: String,
                                 moduleId: String,
                                 eventType: EventPayloadType) =
        mutableListOf<Event>().also { fakeRecords ->
            repeat(nEvents) {
                val subjectId = UUID.randomUUID().toString()
                val eventId = UUID.randomUUID().toString()
                fakeRecords.add(
                    Event(
                        id = eventId,
                        projectId = listOf(projectId),
                        subjectId = listOf(subjectId),
                        attendantId = listOf(userId),
                        moduleId = listOf(moduleId),
                        mode = listOf(Modes.FACE, Modes.FINGERPRINT),
                        payload = buildFakeEventPayload(eventType, subjectId, projectId, userId, moduleId)
                    )
                )
            }
        }

    private fun buildFakeEventPayload(eventType: EventPayloadType, subjectId: String,
                                      projectId: String, userId: String,
                                      moduleId: String) = when(eventType) {
        EventPayloadType.ENROLMENT_RECORD_CREATION -> {
            buildFakeEnrolmentRecordCreation(subjectId, projectId, userId, moduleId)
        }
        EventPayloadType.ENROLMENT_RECORD_DELETION -> {
            buildFakeEnrolmentDeletion(subjectId, projectId, moduleId, userId)
        }
        EventPayloadType.ENROLMENT_RECORD_MOVE -> {
            buildFakeEnrolmentMove(subjectId, projectId, userId, moduleId)
        }
    }

    private fun buildFakeEnrolmentRecordCreation(subjectId: String,
                                                 projectId: String,
                                                 userId: String,
                                                 moduleId: String) = EnrolmentRecordCreationPayload(
        subjectId = subjectId,
        projectId = projectId,
        moduleId = moduleId,
        attendantId = userId,
        biometricReferences = buildFakeBiometricReferences()
    )

    private fun buildFakeBiometricReferences(): List<BiometricReference> {
        val fingerprint = FingerprintGeneratorUtils.generateRandomFingerprint()

        return listOf(
            FaceReference(listOf(FaceTemplate("face_template")), hashMapOf("SDK" to "ML_Kit")),
            FingerprintReference(
                listOf(
                    FingerprintTemplate(fingerprint.templateQualityScore,
                    EncodingUtils.byteArrayToBase64(fingerprint.template), FingerIdentifier.LEFT_3RD_FINGER)
                ),
                hashMapOf("vero" to "VERO_2")
            )
        )
    }

    private fun buildFakeEnrolmentDeletion(subjectId: String,
                                           projectId: String,
                                           attendantId: String,
                                           moduleId: String) = EnrolmentRecordDeletionPayload(
        subjectId, projectId, moduleId, attendantId
    )

    private fun buildFakeEnrolmentMove(subjectId: String,
                                       projectId: String,
                                       userId: String,
                                       moduleId: String) =
        EnrolmentRecordMovePayload(
            buildFakeEnrolmentRecordCreation(subjectId, projectId, userId, moduleId),
            buildFakeEnrolmentDeletion(subjectId, projectId, userId, moduleId)
        )
}
