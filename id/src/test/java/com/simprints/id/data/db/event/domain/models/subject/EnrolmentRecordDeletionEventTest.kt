package com.simprints.id.data.db.event.domain.models.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.event.domain.models.EventLabel.*
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_RECORD_DELETION
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordDeletionEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordDeletionEvent.EnrolmentRecordDeletionPayload
import com.simprints.id.orchestrator.SOME_GUID1
import com.simprints.id.orchestrator.SOME_GUID2
import org.junit.Test

class EnrolmentRecordDeletionEventTest {

    @Test
    fun create_EnrolmentRecordDeletionEvent() {

        val event = EnrolmentRecordDeletionEvent(1, SOME_GUID1, DEFAULT_PROJECT_ID, DEFAULT_MODULE_ID, SOME_GUID2)

        assertThat(event.id).isNotNull()
        assertThat(event.labels).containsExactly(
            ProjectIdLabel(DEFAULT_PROJECT_ID),
            ModuleIdsLabel(listOf(DEFAULT_MODULE_ID)),
            AttendantIdLabel(SOME_GUID2)
        )
        assertThat(event.type).isEqualTo(ENROLMENT_RECORD_DELETION)
        with(event.payload as EnrolmentRecordDeletionPayload) {
            assertThat(createdAt).isEqualTo(1)
            assertThat(endedAt).isEqualTo(0)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(ENROLMENT_RECORD_DELETION)
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(subjectId).isEqualTo(SOME_GUID1)
            assertThat(moduleId).isEqualTo(DEFAULT_MODULE_ID)
            assertThat(attendantId).isEqualTo(SOME_GUID2)
        }
    }
}
