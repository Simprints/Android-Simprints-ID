package com.simprints.infra.events.event.domain.models.callout

import androidx.annotation.Keep
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_CONFIRMATION_V3
import com.simprints.infra.events.event.domain.models.callout.ConfirmationCalloutEventV3.Companion.EVENT_VERSION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_METADATA
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import org.junit.Test

@Keep
class ConfirmationCalloutEventV3Test {
    @Test
    fun create_ConfirmationCalloutEvent() {
        val event = ConfirmationCalloutEventV3(
            createdAt = CREATED_AT,
            projectId = DEFAULT_PROJECT_ID,
            selectedGuid = GUID1,
            sessionId = GUID2,
            metadata = DEFAULT_METADATA
        )

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(CALLOUT_CONFIRMATION_V3)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLOUT_CONFIRMATION_V3)
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(selectedGuid).isEqualTo(GUID1)
            assertThat(sessionId).isEqualTo(GUID2)
        }
    }

    @Test
    fun getTokenizableFields_returnsEmptyMap() {
        val event = ConfirmationCalloutEventV3(
            createdAt = CREATED_AT,
            projectId = DEFAULT_PROJECT_ID,
            selectedGuid = GUID1,
            sessionId = GUID2,
            metadata = DEFAULT_METADATA
        )

        assertThat(event.getTokenizableFields()).isEmpty()
    }
}
