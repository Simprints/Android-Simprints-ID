package com.simprints.infra.eventsync.event.remote.models.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordUpdateEvent
import com.simprints.infra.events.event.domain.models.subject.FaceReference
import com.simprints.infra.events.event.domain.models.subject.FaceTemplate
import com.simprints.infra.events.event.domain.models.subject.FingerprintReference
import com.simprints.infra.events.event.domain.models.subject.FingerprintTemplate
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.face.ApiFaceReference
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.face.ApiFaceTemplate
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintReference
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintTemplate
import org.junit.Test

class ApiEnrolmentRecordUpdateEventTest {
    @Test
    fun convert_EnrolmentRecordUpdateEvent() {
        val apiPayload = ApiEnrolmentRecordUpdatePayload(
            "subjectId",
            listOf(
                ApiFingerprintReference(
                    "fpRefId",
                    listOf(
                        ApiFingerprintTemplate(10, "template", IFingerIdentifier.LEFT_THUMB),
                    ),
                    "NEC_1",
                ),
                ApiFaceReference(
                    "fRefId",
                    listOf(ApiFaceTemplate("template")),
                    "ROC_3",
                ),
            ),
            listOf("fpRefId2"),
        )
        val expectedPayload = EnrolmentRecordUpdateEvent.EnrolmentRecordUpdatePayload(
            "subjectId",
            listOf(
                FingerprintReference(
                    "fpRefId",
                    listOf(FingerprintTemplate(10, "template", IFingerIdentifier.LEFT_THUMB)),
                    "NEC_1",
                ),
                FaceReference(
                    "fRefId",
                    listOf(FaceTemplate("template")),
                    "ROC_3",
                ),
            ),
            listOf("fpRefId2"),
        )

        assertThat(apiPayload.fromApiToDomain()).isEqualTo(expectedPayload)
    }
}
