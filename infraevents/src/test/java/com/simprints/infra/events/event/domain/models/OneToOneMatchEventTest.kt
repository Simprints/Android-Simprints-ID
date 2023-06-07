package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth
import com.simprints.infra.events.event.domain.models.EventType.ONE_TO_ONE_MATCH
import com.simprints.infra.events.event.domain.models.OneToOneMatchEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.ENDED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import org.junit.Test

class OneToOneMatchEventTest {

    @Test
    fun create_OneToOneMatchEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val resultArg = MatchEntry(GUID1, 0F)
        val event = OneToOneMatchEvent(CREATED_AT, ENDED_AT, GUID1, "MATCHER_NAME"
            , resultArg,FingerComparisonStrategy.SAME_FINGER, labels)

        Truth.assertThat(event.id).isNotNull()
        Truth.assertThat(event.labels).isEqualTo(labels)
        Truth.assertThat(event.type).isEqualTo(ONE_TO_ONE_MATCH)
        with(event.payload) {
            Truth.assertThat(createdAt).isEqualTo(CREATED_AT)
            Truth.assertThat(endedAt).isEqualTo(ENDED_AT)
            Truth.assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            Truth.assertThat(matcher).isEqualTo("MATCHER_NAME")
            Truth.assertThat(fingerComparisonStrategy).isEqualTo(FingerComparisonStrategy.SAME_FINGER)
            Truth.assertThat(type).isEqualTo(ONE_TO_ONE_MATCH)
            Truth.assertThat(result).isEqualTo(resultArg)
        }
    }
}

