package com.simprints.infra.eventsync.event.remote.models.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.infra.events.event.domain.models.subject.FingerprintReference
import com.simprints.infra.events.event.domain.models.subject.FingerprintTemplate
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintReference
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintTemplate
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
                            ApiFingerprintTemplate(10, "template", IFingerIdentifier.LEFT_THUMB),
                        ),
                        "NEC_1",
                    ),
                ),
            ),
            ApiEnrolmentRecordMovePayload.ApiEnrolmentRecordDeletionInMove(
                "subjectId",
                "projectId",
                "moduleId2",
                "attendantId",
            ),
        )
        val expectedPayload = EnrolmentRecordMoveEvent.EnrolmentRecordMovePayload(
            EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove(
                "subjectId",
                "projectId",
                "moduleId1".asTokenizableEncrypted(),
                "attendantId".asTokenizableEncrypted(),
                listOf(
                    FingerprintReference(
                        "fpRefId",
                        listOf(
                            FingerprintTemplate(10, "template", IFingerIdentifier.LEFT_THUMB),
                        ),
                        "NEC_1",
                    ),
                ),
            ),
            EnrolmentRecordMoveEvent.EnrolmentRecordDeletionInMove(
                "subjectId",
                "projectId",
                "moduleId2".asTokenizableEncrypted(),
                "attendantId".asTokenizableEncrypted(),
            ),
        )

        assertThat(apiPayload.fromApiToDomain()).isEqualTo(expectedPayload)
    }
}
