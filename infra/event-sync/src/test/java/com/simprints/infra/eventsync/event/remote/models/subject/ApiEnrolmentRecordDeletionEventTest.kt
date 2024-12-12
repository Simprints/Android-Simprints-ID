package com.simprints.infra.eventsync.event.remote.models.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordDeletionEvent
import org.junit.Test

class ApiEnrolmentRecordDeletionEventTest {
    @Test
    fun convert_EnrolmentRecordDeletionEvent() {
        val apiPayload = ApiEnrolmentRecordDeletionPayload(
            "subjectId",
            "projectId",
            "moduleId",
            "attendantId",
        )
        val expectedPayload = EnrolmentRecordDeletionEvent.EnrolmentRecordDeletionPayload(
            "subjectId",
            "projectId",
            "moduleId",
            "attendantId",
        )

        assertThat(apiPayload.fromApiToDomain()).isEqualTo(expectedPayload)
    }
}
