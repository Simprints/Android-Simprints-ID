package com.simprints.id.data.db.event.domain.models.callback

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.event.domain.models.CREATED_AT
import com.simprints.id.data.db.event.domain.models.DEFAULT_ENDED_AT
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_ENROLMENT
import com.simprints.id.data.db.event.domain.models.callback.EnrolmentCallbackEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.callback.EnrolmentCallbackEvent.EnrolmentCallbackPayload
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test

class EnrolmentCallbackEventTest {

    @Test
    fun create_EnrolmentCallbackEvent() {
        val event = EnrolmentCallbackEvent(CREATED_AT, SOME_GUID1, SOME_GUID1)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).containsExactly(SessionIdLabel(SOME_GUID1))
        assertThat(event.type).isEqualTo(CALLBACK_ENROLMENT)
        with(event.payload as EnrolmentCallbackPayload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLBACK_ENROLMENT)
            assertThat(guid).isEqualTo(SOME_GUID1)
        }
    }
}
