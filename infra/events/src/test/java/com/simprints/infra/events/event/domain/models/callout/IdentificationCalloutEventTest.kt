package com.simprints.infra.events.event.domain.models.callout

import androidx.annotation.Keep
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_IDENTIFICATION
import com.simprints.infra.events.event.domain.models.callout.IdentificationCalloutEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_METADATA
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import org.junit.Test

@Keep
class IdentificationCalloutEventTest {
    @Test
    fun create_IdentificationCalloutEvent() {
        val event = IdentificationCalloutEvent(
            createdAt = CREATED_AT,
            projectId = DEFAULT_PROJECT_ID,
            userId = DEFAULT_USER_ID,
            moduleId = DEFAULT_MODULE_ID,
            metadata = DEFAULT_METADATA,
        )

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(CALLOUT_IDENTIFICATION)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLOUT_IDENTIFICATION)
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(userId).isEqualTo(DEFAULT_USER_ID)
            assertThat(moduleId).isEqualTo(DEFAULT_MODULE_ID)
            assertThat(metadata).isEqualTo(DEFAULT_METADATA)
        }
    }
}
