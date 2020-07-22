package com.simprints.id.data.db.event.domain.models.callback

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_CONFIRMATION
import com.simprints.id.data.db.event.domain.models.callback.ConfirmationCallbackEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.callback.ConfirmationCallbackEvent.ConfirmationCallbackPayload
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test

class ConfirmationCallbackEventTest {

    @Test
    fun create_ConfirmationCallbackEvent() {
        val event = ConfirmationCallbackEvent(0, true, SOME_GUID1)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).containsExactly(SessionIdLabel(SOME_GUID1))
        assertThat(event.type).isEqualTo(CALLBACK_CONFIRMATION)
        with(event.payload as ConfirmationCallbackPayload) {
            assertThat(createdAt).isEqualTo(0)
            assertThat(endedAt).isEqualTo(0)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLBACK_CONFIRMATION)
            assertThat(identificationOutcome).isEqualTo(true)
        }
    }
}
