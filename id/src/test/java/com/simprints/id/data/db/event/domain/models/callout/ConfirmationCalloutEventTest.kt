package com.simprints.id.data.db.event.domain.models.callout

import androidx.annotation.Keep
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_CONFIRMATION
import com.simprints.id.data.db.event.domain.models.callout.ConfirmationCalloutEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.callout.ConfirmationCalloutEvent.ConfirmationCalloutPayload
import com.simprints.id.orchestrator.SOME_GUID1
import com.simprints.id.orchestrator.SOME_GUID2
import org.junit.Test

@Keep
class ConfirmationCalloutEventTest {
    @Test
    fun create_ConfirmationCalloutEvent() {

        val event = ConfirmationCalloutEvent(0, DEFAULT_PROJECT_ID, SOME_GUID1, SOME_GUID2)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).containsExactly(SessionIdLabel(SOME_GUID2))
        assertThat(event.type).isEqualTo(CALLBACK_CONFIRMATION)
        with(event.payload as ConfirmationCalloutPayload) {
            assertThat(createdAt).isEqualTo(0)
            assertThat(endedAt).isEqualTo(0)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLBACK_CONFIRMATION)
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(selectedGuid).isEqualTo(SOME_GUID1)
            assertThat(sessionId).isEqualTo(SOME_GUID2)
        }
    }
}
