package com.simprints.id.data.db.event.domain.models.callback

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_IDENTIFICATION
import com.simprints.id.data.db.event.domain.models.callback.IdentificationCallbackEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.callback.IdentificationCallbackEvent.IdentificationCallbackPayload
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier.TIER_1
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test

class IdentificationCallbackEventTest {

    @Test
    fun create_IdentificationCallbackEvent() {
        val comparisonScore = CallbackComparisonScore(SOME_GUID1, 1, TIER_1)

        val event = IdentificationCallbackEvent(1, SOME_GUID1, listOf(comparisonScore))
        assertThat(event.id).isNotNull()
        assertThat(event.labels).containsExactly(SessionIdLabel(SOME_GUID1))
        assertThat(event.type).isEqualTo(CALLBACK_IDENTIFICATION)
        with(event.payload as IdentificationCallbackPayload) {
            assertThat(createdAt).isEqualTo(1)
            assertThat(endedAt).isEqualTo(0)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLBACK_IDENTIFICATION)
            assertThat(scores).containsExactly(comparisonScore)
        }
    }
}
