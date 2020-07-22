package com.simprints.id.data.db.event.domain.models.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.event.domain.models.EventLabel.*
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_RECORD_MOVE
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordDeletionEvent.EnrolmentRecordDeletionPayload
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordMovePayload
import com.simprints.id.data.db.event.domain.models.subject.FingerIdentifier.LEFT_3RD_FINGER
import com.simprints.id.domain.modality.Modes.FACE
import com.simprints.id.domain.modality.Modes.FINGERPRINT
import com.simprints.id.orchestrator.SOME_GUID1
import com.simprints.id.orchestrator.SOME_GUID2
import org.junit.Test

class EnrolmentRecordMoveEventTest {

    @Test
    fun create_EnrolmentRecordMoveEvent() {
        val creation = createCreationPayload()
        val deletion = createDeletionPayload()
        val event = EnrolmentRecordMoveEvent(1, creation, deletion)

        assertThat(event.id).isNotNull()
        assertThat(event.labels).containsExactly(
            ProjectIdLabel(DEFAULT_PROJECT_ID),
            ModuleIdsLabel(listOf(DEFAULT_MODULE_ID)),
            AttendantIdLabel(SOME_GUID2)
        )
        assertThat(event.type).isEqualTo(ENROLMENT_RECORD_MOVE)
        with(event.payload as EnrolmentRecordMovePayload) {
            assertThat(createdAt).isEqualTo(1)
            assertThat(endedAt).isEqualTo(0)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(ENROLMENT_RECORD_MOVE)
            assertThat(enrolmentRecordCreation).isEqualTo(creation)
            assertThat(enrolmentRecordDeletion).isEqualTo(deletion)
        }
    }

    private fun createCreationPayload(): EnrolmentRecordCreationPayload {
        val fingerprintReference = FingerprintReference(listOf(FingerprintTemplate(0, "some_template", LEFT_3RD_FINGER)), hashMapOf("some_key" to "some_value"))
        val faceReference = FaceReference(listOf(FaceTemplate("some_template")))
        return EnrolmentRecordCreationEvent(
            1,
            SOME_GUID1,
            DEFAULT_PROJECT_ID,
            DEFAULT_MODULE_ID,
            SOME_GUID2,
            listOf(FACE, FINGERPRINT),
            listOf(fingerprintReference,
                faceReference)
        ).payload as EnrolmentRecordCreationPayload
    }

    private fun createDeletionPayload(): EnrolmentRecordDeletionPayload =
        EnrolmentRecordDeletionEvent(1, SOME_GUID1, DEFAULT_PROJECT_ID, DEFAULT_MODULE_ID, SOME_GUID2).payload as EnrolmentRecordDeletionPayload
}
