package com.simprints.id.data.db.event.domain.models.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.event.domain.models.CREATED_AT
import com.simprints.id.data.db.event.domain.models.DEFAULT_ENDED_AT
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_RECORD_CREATION
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.subject.FingerIdentifier.LEFT_3RD_FINGER
import com.simprints.id.domain.modality.Modes.FACE
import com.simprints.id.domain.modality.Modes.FINGERPRINT
import
import 
import org.junit.Test

class EnrolmentRecordCreationEventTest {

    @Test
    fun create_EnrolmentRecordCreationEvent() {
        val fingerprintReference = FingerprintReference(listOf(FingerprintTemplate(0, "some_template", LEFT_3RD_FINGER)), hashMapOf("some_key" to "some_value"))
        val faceReference = FaceReference(listOf(FaceTemplate("some_template")))
        val event = EnrolmentRecordCreationEvent(
            CREATED_AT, GUID1, DEFAULT_PROJECT_ID, DEFAULT_MODULE_ID, GUID2, listOf(FACE, FINGERPRINT), listOf(fingerprintReference, faceReference))

        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(EventLabels())
        assertThat(event.type).isEqualTo(ENROLMENT_RECORD_CREATION)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(ENROLMENT_RECORD_CREATION)
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(subjectId).isEqualTo(GUID1)
            assertThat(moduleId).isEqualTo(DEFAULT_MODULE_ID)
            assertThat(attendantId).isEqualTo(GUID2)
            assertThat(biometricReferences).containsExactly(fingerprintReference, faceReference)
        }
    }
}
