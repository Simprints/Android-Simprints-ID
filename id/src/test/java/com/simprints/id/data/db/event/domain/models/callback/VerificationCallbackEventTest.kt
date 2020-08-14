package com.simprints.id.data.db.event.domain.models.callback

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.event.domain.models.CREATED_AT
import com.simprints.id.data.db.event.domain.models.DEFAULT_ENDED_AT
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_VERIFICATION
import com.simprints.id.data.db.event.domain.models.callback.VerificationCallbackEvent.Companion.EVENT_VERSION
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier.TIER_1
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test

class VerificationCallbackEventTest {

    @Test
    fun create_VerificationCallbackEvent() {
        val labels = EventLabels(sessionId = SOME_GUID1)
        val comparisonScore = CallbackComparisonScore(SOME_GUID1, 1, TIER_1)

        val event = VerificationCallbackEvent(CREATED_AT, comparisonScore, labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(CALLBACK_VERIFICATION)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLBACK_VERIFICATION)
            assertThat(comparisonScore).isEqualTo(comparisonScore)
        }
    }
}
