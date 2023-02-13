package com.simprints.eventsystem.event.remote.models.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.eventsystem.event.domain.models.subject.FingerprintReference
import com.simprints.eventsystem.event.domain.models.subject.FingerprintTemplate
import com.simprints.eventsystem.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintReference
import com.simprints.eventsystem.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintTemplate
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import org.junit.Test

class ApiEnrolmentRecordMoveEventTest {

    @Test
    fun convert_EnrolmentRecordMoveEvent() {
        val apiPayload = ApiEnrolmentRecordMovePayload(
            ApiEnrolmentRecordMovePayload.ApiEnrolmentRecordCreationInMove(
                "subjectId",
                "projectId",
                "moduleId1",
                "attendantId",
                listOf(
                    ApiFingerprintReference(
                        "fpRefId",
                        listOf(
                            ApiFingerprintTemplate(10, "template", IFingerIdentifier.LEFT_THUMB)
                        ),
                        FingerprintTemplateFormat.NEC,
                    )
                )
            ),
            ApiEnrolmentRecordMovePayload.ApiEnrolmentRecordDeletionInMove(
                "subjectId",
                "projectId",
                "moduleId2",
                "attendantId",
            )
        )
        val expectedPayload = EnrolmentRecordMoveEvent.EnrolmentRecordMovePayload(
            EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove(
                "subjectId",
                "projectId",
                "moduleId1",
                "attendantId",
                listOf(
                    FingerprintReference(
                        "fpRefId",
                        listOf(
                            FingerprintTemplate(10, "template", IFingerIdentifier.LEFT_THUMB)
                        ),
                        FingerprintTemplateFormat.NEC,
                    )
                )
            ),
            EnrolmentRecordMoveEvent.EnrolmentRecordDeletionInMove(
                "subjectId",
                "projectId",
                "moduleId2",
                "attendantId",
            )
        )

        assertThat(apiPayload.fromApiToDomain()).isEqualTo(expectedPayload)
    }
}
