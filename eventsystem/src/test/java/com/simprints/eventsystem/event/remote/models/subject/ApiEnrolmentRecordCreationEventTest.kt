package com.simprints.eventsystem.event.remote.models.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.eventsystem.event.domain.models.subject.FingerprintReference
import com.simprints.eventsystem.event.domain.models.subject.FingerprintTemplate
import com.simprints.eventsystem.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintReference
import com.simprints.eventsystem.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintTemplate
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
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
                        ApiFingerprintTemplate(10, "template", IFingerIdentifier.LEFT_THUMB)
                    ),
                    FingerprintTemplateFormat.NEC_1,
                )
            )
        )
        val expectedPayload = EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload(
            "subjectId",
            "projectId",
            "moduleId",
            "attendantId",
            listOf(
                FingerprintReference(
                    "fpRefId",
                    listOf(
                        FingerprintTemplate(10, "template", IFingerIdentifier.LEFT_THUMB)
                    ),
                    FingerprintTemplateFormat.NEC_1,
                )
            )
        )

        assertThat(apiPayload.fromApiToDomain()).isEqualTo(expectedPayload)
    }
}
