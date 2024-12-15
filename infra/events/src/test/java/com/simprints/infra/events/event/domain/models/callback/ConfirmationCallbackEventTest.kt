package com.simprints.infra.events.event.domain.models.callback

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_CONFIRMATION
import com.simprints.infra.events.event.domain.models.callback.ConfirmationCallbackEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import org.junit.Test

class ConfirmationCallbackEventTest {
    @Test
    fun create_ConfirmationCallbackEvent() {
        val event = ConfirmationCallbackEvent(CREATED_AT, true)

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(CALLBACK_CONFIRMATION)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isNull()
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLBACK_CONFIRMATION)
            assertThat(identificationOutcome).isEqualTo(true)
        }
    }
}
