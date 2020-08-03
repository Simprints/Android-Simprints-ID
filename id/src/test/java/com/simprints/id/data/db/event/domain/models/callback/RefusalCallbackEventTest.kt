package com.simprints.id.data.db.event.domain.models.callback
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.events.CREATED_AT
import com.simprints.id.commontesttools.events.DEFAULT_ENDED_AT
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_REFUSAL
import com.simprints.id.data.db.event.domain.models.callback.RefusalCallbackEvent.Companion.EVENT_VERSION
import org.junit.Test

class RefusalCallbackEventTest {

    @Test
    fun create_RefusalCallbackEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val event = RefusalCallbackEvent(CREATED_AT, "some_reason", "some_extra", labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(CALLBACK_REFUSAL)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLBACK_REFUSAL)
            assertThat(reason).isEqualTo("some_reason")
            assertThat(extra).isEqualTo("some_extra")
        }
    }
}
