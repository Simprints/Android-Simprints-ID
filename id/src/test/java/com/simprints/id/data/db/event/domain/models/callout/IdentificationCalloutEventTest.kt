package com.simprints.eventsystem.event.domain.models.callout

import androidx.annotation.Keep
import com.google.common.truth.Truth.assertThat
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_METADATA
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.id.sampledata.SampleDefaults.GUID1
import com.simprints.id.sampledata.SampleDefaults.CREATED_AT
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_ENDED_AT
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventType.CALLOUT_IDENTIFICATION
import com.simprints.eventsystem.event.domain.models.callout.IdentificationCalloutEvent.Companion.EVENT_VERSION
import org.junit.Test

@Keep
class IdentificationCalloutEventTest {
    @Test
    fun create_IdentificationCalloutEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val event = IdentificationCalloutEvent(CREATED_AT, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA, labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(CALLOUT_IDENTIFICATION)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLOUT_IDENTIFICATION)
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(userId).isEqualTo(DEFAULT_USER_ID)
            assertThat(moduleId).isEqualTo(DEFAULT_MODULE_ID)
            assertThat(metadata).isEqualTo(DEFAULT_METADATA)
        }
    }
}

