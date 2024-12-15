package com.simprints.infra.eventsync.event.remote.models.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.subject.FingerprintReference
import com.simprints.infra.events.event.domain.models.subject.FingerprintTemplate
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintReference
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintTemplate
import org.junit.Test

class ApiEnrolmentRecordCreationEventTest {
    @Test
    fun convert_EnrolmentRecordCreationEvent() {
        val apiPayload = ApiEnrolmentRecordCreationPayload(
            "subjectId",
            "projectId",
            "moduleId",
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
        )
        val expectedPayload = EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload(
            "subjectId",
            "projectId",
            "moduleId".asTokenizableEncrypted(),
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
        )

        assertThat(apiPayload.fromApiToDomain()).isEqualTo(expectedPayload)
    }
}
