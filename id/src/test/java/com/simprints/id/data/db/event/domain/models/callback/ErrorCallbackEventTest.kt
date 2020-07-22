package com.simprints.id.data.db.event.domain.models.callback

import com.google.common.truth.Truth
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_ERROR
import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.DIFFERENT_PROJECT_ID_SIGNED_IN
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test
import com.google.common.truth.Truth.*

class ErrorCallbackEventTest {

    @Test
    fun create_ErrorCallbackEvent() {
        val event = ErrorCallbackEvent(1, DIFFERENT_PROJECT_ID_SIGNED_IN, SOME_GUID1)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).containsExactly(SessionIdLabel(SOME_GUID1))
        assertThat(event.type).isEqualTo(CALLBACK_ERROR)
        with(event.payload as ErrorCallbackPayload) {
            assertThat(createdAt).isEqualTo(1)
            assertThat(endedAt).isEqualTo(0)
            assertThat(eventVersion).isEqualTo(ErrorCallbackEvent.EVENT_VERSION)
            assertThat(type).isEqualTo(CALLBACK_ERROR)
            assertThat(reason).isEqualTo(DIFFERENT_PROJECT_ID_SIGNED_IN)
        }
    }
}
