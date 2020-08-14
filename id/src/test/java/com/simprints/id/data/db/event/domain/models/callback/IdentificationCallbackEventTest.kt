package com.simprints.id.data.db.event.domain.models.callback

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.event.domain.models.CREATED_AT
import com.simprints.id.data.db.event.domain.models.DEFAULT_ENDED_AT
import com.simprints.id.data.db.event.domain.models.EventLabels

import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_IDENTIFICATION
import com.simprints.id.data.db.event.domain.models.callback.IdentificationCallbackEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.callback.IdentificationCallbackEvent.IdentificationCallbackPayload
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier.TIER_1
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test

class IdentificationCallbackEventTest {

    @Test
    fun create_IdentificationCallbackEvent() {
        val labels = EventLabels(sessionId = SOME_GUID1)
        val comparisonScore = CallbackComparisonScore(SOME_GUID1, 1, TIER_1)

        val event = IdentificationCallbackEvent(CREATED_AT, SOME_GUID1, listOf(comparisonScore), labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(CALLBACK_IDENTIFICATION)
        with(event.payload as IdentificationCallbackPayload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLBACK_IDENTIFICATION)
            assertThat(scores).containsExactly(comparisonScore)
        }
    }
}
