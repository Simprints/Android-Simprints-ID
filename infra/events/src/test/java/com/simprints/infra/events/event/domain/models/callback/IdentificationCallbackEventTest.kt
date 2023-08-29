package com.simprints.infra.events.event.domain.models.callback

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_IDENTIFICATION
import com.simprints.infra.events.event.domain.models.callback.IdentificationCallbackEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_ENDED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.moduleapi.app.responses.IAppMatchConfidence
import com.simprints.moduleapi.app.responses.IAppResponseTier.TIER_1
import org.junit.Test

class IdentificationCallbackEventTest {

    @Test
    fun create_IdentificationCallbackEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val comparisonScore = CallbackComparisonScore(GUID1, 1, TIER_1, IAppMatchConfidence.NONE)

        val event = IdentificationCallbackEvent(CREATED_AT, GUID1, listOf(comparisonScore), labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(CALLBACK_IDENTIFICATION)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLBACK_IDENTIFICATION)
            assertThat(scores).containsExactly(comparisonScore)
        }
    }
}
