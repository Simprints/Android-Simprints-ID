package com.simprints.id.data.db.event.domain.models.callout

import androidx.annotation.Keep
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.event.domain.models.CREATED_AT
import com.simprints.id.data.db.event.domain.models.DEFAULT_ENDED_AT
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType.CALLOUT_CONFIRMATION
import com.simprints.id.data.db.event.domain.models.callout.ConfirmationCalloutEvent.Companion.EVENT_VERSION
import com.simprints.id.orchestrator.SOME_GUID1
import com.simprints.id.orchestrator.SOME_GUID2
import org.junit.Test

@Keep
class ConfirmationCalloutEventTest {
    @Test
    fun create_ConfirmationCalloutEvent() {
        val labels = EventLabels(sessionId = SOME_GUID1)
        val event = ConfirmationCalloutEvent(CREATED_AT, DEFAULT_PROJECT_ID, SOME_GUID1, SOME_GUID2, labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(CALLOUT_CONFIRMATION)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLOUT_CONFIRMATION)
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(selectedGuid).isEqualTo(SOME_GUID1)
            assertThat(sessionId).isEqualTo(SOME_GUID2)
        }
    }
}
