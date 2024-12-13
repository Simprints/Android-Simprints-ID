package com.simprints.infra.events.event.domain.models.callback

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_ENROLMENT
import com.simprints.infra.events.event.domain.models.callback.EnrolmentCallbackEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import org.junit.Test

class EnrolmentCallbackEventTest {
    @Test
    fun create_EnrolmentCallbackEvent() {
        val event = EnrolmentCallbackEvent(CREATED_AT, GUID1)

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(CALLBACK_ENROLMENT)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLBACK_ENROLMENT)
            assertThat(guid).isEqualTo(GUID1)
        }
    }
}
