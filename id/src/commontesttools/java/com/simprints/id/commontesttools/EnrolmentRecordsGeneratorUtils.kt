package com.simprints.id.commontesttools

import com.simprints.id.data.db.person.domain.personevents.*
import com.simprints.id.domain.modality.Modes
import java.util.*

object EnrolmentRecordsGeneratorUtils {
    fun getRandomEnrolmentEvents(nPeople: Int,
                                 projectId: String,
                                 userId: String,
                                 moduleId: String) =
        mutableListOf<Event>().also { fakeRecords ->
            repeat(nPeople) {
                val subjectId = UUID.randomUUID().toString()
                val eventId = UUID.randomUUID().toString()
                fakeRecords.add(
                    Event(
                        eventId,
                        listOf(projectId),
                        listOf(subjectId),
                        listOf(userId),
                        listOf(moduleId),
                        listOf(Modes.FACE, Modes.FINGERPRINT),
                        buildFakeEnrolmentRecordCreation(subjectId, projectId, userId, moduleId)
                    )
                )
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
                    fingerprint.template.toString(), FingerIdentifier.LEFT_3RD_FINGER)
                ),
                hashMapOf("vero" to "VERO_2")
            )
        )
    }
}
