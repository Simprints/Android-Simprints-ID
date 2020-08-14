package com.simprints.id.data.db.event.domain.models.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.event.domain.models.CREATED_AT
import com.simprints.id.data.db.event.domain.models.DEFAULT_ENDED_AT
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_RECORD_DELETION
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordDeletionEvent.Companion.EVENT_VERSION
import com.simprints.id.orchestrator.SOME_GUID1
import com.simprints.id.orchestrator.SOME_GUID2
import org.junit.Test

class EnrolmentRecordDeletionEventTest {

    @Test
    fun create_EnrolmentRecordDeletionEvent() {
        val event = EnrolmentRecordDeletionEvent(CREATED_AT, SOME_GUID1, DEFAULT_PROJECT_ID, DEFAULT_MODULE_ID, SOME_GUID2)

        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(EventLabels())
        assertThat(event.type).isEqualTo(ENROLMENT_RECORD_DELETION)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(ENROLMENT_RECORD_DELETION)
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(subjectId).isEqualTo(SOME_GUID1)
            assertThat(moduleId).isEqualTo(DEFAULT_MODULE_ID)
            assertThat(attendantId).isEqualTo(SOME_GUID2)
        }
    }
}
