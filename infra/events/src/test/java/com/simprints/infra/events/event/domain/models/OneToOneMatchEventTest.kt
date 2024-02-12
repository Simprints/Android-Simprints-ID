package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.EventType.ONE_TO_ONE_MATCH
import com.simprints.infra.events.event.domain.models.OneToOneMatchEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.ENDED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import org.junit.Test

class OneToOneMatchEventTest {

    @Test
    fun create_OneToOneMatchEvent() {
        val resultArg = MatchEntry(GUID1, 0F)
        val event = OneToOneMatchEvent(
            CREATED_AT,
            ENDED_AT,
            GUID1,
            "MATCHER_NAME",
            resultArg,
            FingerComparisonStrategy.SAME_FINGER
        )

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(ONE_TO_ONE_MATCH)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(matcher).isEqualTo("MATCHER_NAME")
            assertThat(fingerComparisonStrategy)
                .isEqualTo(FingerComparisonStrategy.SAME_FINGER)
            assertThat(type).isEqualTo(ONE_TO_ONE_MATCH)
            assertThat(result).isEqualTo(resultArg)
        }
    }
}

