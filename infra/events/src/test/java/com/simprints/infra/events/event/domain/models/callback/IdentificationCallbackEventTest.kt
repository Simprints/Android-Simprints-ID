package com.simprints.infra.events.event.domain.models.callback

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.response.AppMatchConfidence
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_IDENTIFICATION
import com.simprints.infra.events.event.domain.models.callback.IdentificationCallbackEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import org.junit.Test

class IdentificationCallbackEventTest {
    @Test
    fun create_IdentificationCallbackEvent() {
        val comparisonScore = CallbackComparisonScore(GUID1, 1, AppMatchConfidence.NONE)

        val event = IdentificationCallbackEvent(CREATED_AT, GUID1, listOf(comparisonScore))
        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(CALLBACK_IDENTIFICATION)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isNull()
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLBACK_IDENTIFICATION)
            assertThat(scores).containsExactly(comparisonScore)
        }
    }
}
