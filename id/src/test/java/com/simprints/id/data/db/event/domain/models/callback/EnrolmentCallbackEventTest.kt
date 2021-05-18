package com.simprints.eventsystem.event.domain.models.callback

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.EventLabels

import com.simprints.eventsystem.event.domain.models.EventType.CALLBACK_ENROLMENT
import com.simprints.eventsystem.event.domain.models.callback.EnrolmentCallbackEvent.Companion.EVENT_VERSION
import com.simprints.eventsystem.event.domain.models.callback.EnrolmentCallbackEvent.EnrolmentCallbackPayload
import com.simprints.id.sampledata.SampleDefaults.GUID1
import com.simprints.id.sampledata.SampleDefaults.CREATED_AT
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_ENDED_AT
import org.junit.Test

class EnrolmentCallbackEventTest {

    @Test
    fun create_EnrolmentCallbackEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val event = EnrolmentCallbackEvent(CREATED_AT, GUID1, labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(CALLBACK_ENROLMENT)
        with(event.payload as EnrolmentCallbackPayload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLBACK_ENROLMENT)
            assertThat(guid).isEqualTo(GUID1)
        }
    }
}
