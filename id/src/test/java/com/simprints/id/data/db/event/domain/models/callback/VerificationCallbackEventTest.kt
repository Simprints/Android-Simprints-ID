package com.simprints.id.data.db.event.domain.models.callback

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_VERIFICATION
import com.simprints.id.data.db.event.domain.models.callback.VerificationCallbackEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.callback.VerificationCallbackEvent.VerificationCallbackPayload
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier.TIER_1
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test

class VerificationCallbackEventTest {

    @Test
    fun create_VerificationCallbackEvent() {
        val comparisonScore = CallbackComparisonScore(SOME_GUID1, 1, TIER_1)

        val event = VerificationCallbackEvent(1, comparisonScore, SOME_GUID1)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).containsExactly(SessionIdLabel(SOME_GUID1))
        assertThat(event.type).isEqualTo(CALLBACK_VERIFICATION)
        with(event.payload as VerificationCallbackPayload) {
            assertThat(createdAt).isEqualTo(1)
            assertThat(endedAt).isEqualTo(0)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLBACK_VERIFICATION)
            assertThat(comparisonScore).isEqualTo(comparisonScore)
        }
    }
}
