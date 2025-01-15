package com.simprints.infra.events.event.domain.models.callback

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.response.AppMatchConfidence
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_VERIFICATION
import com.simprints.infra.events.event.domain.models.callback.VerificationCallbackEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import org.junit.Test

class VerificationCallbackEventTest {
    @Test
    fun create_VerificationCallbackEvent() {
        val comparisonScore = CallbackComparisonScore(GUID1, 1, AppMatchConfidence.NONE)

        val event = VerificationCallbackEvent(CREATED_AT, comparisonScore)
        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(CALLBACK_VERIFICATION)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CALLBACK_VERIFICATION)
            assertThat(comparisonScore).isEqualTo(comparisonScore)
        }
    }
}
