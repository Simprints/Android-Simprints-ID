package com.simprints.infra.events.event.domain.models.callback

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_REFUSAL
import com.simprints.infra.events.event.domain.models.callback.RefusalCallbackEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import org.junit.Test

class RefusalCallbackEventTest {
    @Test
    fun create_RefusalCallbackEvent() {
        val event = RefusalCallbackEvent(CREATED_AT, "some_reason", "some_extra")

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(CALLBACK_REFUSAL)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLBACK_REFUSAL)
            assertThat(reason).isEqualTo("some_reason")
            assertThat(extra).isEqualTo("some_extra")
        }
    }
}
