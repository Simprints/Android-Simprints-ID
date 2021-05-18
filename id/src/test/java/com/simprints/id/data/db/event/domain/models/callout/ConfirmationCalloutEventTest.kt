package com.simprints.eventsystem.event.domain.models.callout

import androidx.annotation.Keep
import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventType.CALLOUT_CONFIRMATION
import com.simprints.eventsystem.event.domain.models.callout.ConfirmationCalloutEvent.Companion.EVENT_VERSION
import com.simprints.id.sampledata.SampleDefaults.CREATED_AT
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_ENDED_AT
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.id.sampledata.SampleDefaults.GUID1
import com.simprints.id.sampledata.SampleDefaults.GUID2
import org.junit.Test

@Keep
class ConfirmationCalloutEventTest {
    @Test
    fun create_ConfirmationCalloutEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val event = ConfirmationCalloutEvent(CREATED_AT, DEFAULT_PROJECT_ID, GUID1, GUID2, labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(CALLOUT_CONFIRMATION)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLOUT_CONFIRMATION)
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(selectedGuid).isEqualTo(GUID1)
            assertThat(sessionId).isEqualTo(GUID2)
        }
    }
}
