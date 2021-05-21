package com.simprints.eventsystem.event.domain.models.callback

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.EventLabels

import com.simprints.eventsystem.event.domain.models.EventType.CALLBACK_ERROR
import com.simprints.eventsystem.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload
import com.simprints.eventsystem.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.DIFFERENT_PROJECT_ID_SIGNED_IN
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.eventsystem.sampledata.SampleDefaults.CREATED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_ENDED_AT
import org.junit.Test

class ErrorCallbackEventTest {

    @Test
    fun create_ErrorCallbackEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val event = ErrorCallbackEvent(CREATED_AT, DIFFERENT_PROJECT_ID_SIGNED_IN, labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(CALLBACK_ERROR)
        with(event.payload as ErrorCallbackPayload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(ErrorCallbackEvent.EVENT_VERSION)
            assertThat(type).isEqualTo(CALLBACK_ERROR)
            assertThat(reason).isEqualTo(DIFFERENT_PROJECT_ID_SIGNED_IN)
        }
    }
}
