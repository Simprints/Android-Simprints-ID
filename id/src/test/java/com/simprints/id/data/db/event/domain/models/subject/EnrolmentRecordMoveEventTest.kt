package com.simprints.eventsystem.event.domain.models.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventType.ENROLMENT_RECORD_MOVE
import com.simprints.eventsystem.event.domain.models.face.FaceTemplateFormat
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordMoveEvent.Companion.EVENT_VERSION
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordDeletionInMove
import com.simprints.eventsystem.event.domain.models.subject.FingerIdentifier.LEFT_3RD_FINGER
import com.simprints.id.sampledata.SampleDefaults.CREATED_AT
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_ENDED_AT
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.id.sampledata.SampleDefaults.GUID1
import com.simprints.id.sampledata.SampleDefaults.GUID2
import org.junit.Test

class EnrolmentRecordMoveEventTest {

    @Test
    fun create_EnrolmentRecordMoveEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val creation = EnrolmentRecordCreationInMove(
            GUID1,
            DEFAULT_PROJECT_ID,
            DEFAULT_MODULE_ID,
            DEFAULT_USER_ID,
            createBiometricReferences()
        )
        val deletion = EnrolmentRecordDeletionInMove(GUID1, DEFAULT_PROJECT_ID, DEFAULT_MODULE_ID, DEFAULT_USER_ID)
        val event = EnrolmentRecordMoveEvent(CREATED_AT, creation, deletion, labels)

        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(ENROLMENT_RECORD_MOVE)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(ENROLMENT_RECORD_MOVE)
            assertThat(enrolmentRecordCreation).isEqualTo(creation)
            assertThat(enrolmentRecordDeletion).isEqualTo(deletion)
        }
    }

    private fun createBiometricReferences(): List<BiometricReference> {
        val fingerprintReference = FingerprintReference(
            GUID1,
            listOf(FingerprintTemplate(0, "some_template", LEFT_3RD_FINGER)),
            FingerprintTemplateFormat.ISO_19794_2,
            hashMapOf("some_key" to "some_value")
        )
        val faceReference =
            FaceReference(GUID2, listOf(FaceTemplate("some_template")), FaceTemplateFormat.RANK_ONE_1_23)
        return listOf(fingerprintReference, faceReference)
    }
}
