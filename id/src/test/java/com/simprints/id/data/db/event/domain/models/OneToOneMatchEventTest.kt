package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth
import com.simprints.id.sampledata.DefaultTestConstants.GUID1
import com.simprints.id.sampledata.DefaultTestConstants.CREATED_AT
import com.simprints.id.sampledata.DefaultTestConstants.ENDED_AT
import com.simprints.id.data.db.event.domain.models.EventType.ONE_TO_ONE_MATCH
import com.simprints.id.data.db.event.domain.models.Matcher.RANK_ONE
import com.simprints.id.data.db.event.domain.models.OneToOneMatchEvent.Companion.EVENT_VERSION
import org.junit.Test

class OneToOneMatchEventTest {

    @Test
    fun create_OneToOneMatchEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val resultArg = MatchEntry(GUID1, 0F)
        val event = OneToOneMatchEvent(CREATED_AT, ENDED_AT, GUID1, RANK_ONE, resultArg, labels)

        Truth.assertThat(event.id).isNotNull()
        Truth.assertThat(event.labels).isEqualTo(labels)
        Truth.assertThat(event.type).isEqualTo(ONE_TO_ONE_MATCH)
        with(event.payload) {
            Truth.assertThat(createdAt).isEqualTo(CREATED_AT)
            Truth.assertThat(endedAt).isEqualTo(ENDED_AT)
            Truth.assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            Truth.assertThat(matcher).isEqualTo(RANK_ONE)
            Truth.assertThat(type).isEqualTo(ONE_TO_ONE_MATCH)
            Truth.assertThat(result).isEqualTo(resultArg)
        }
    }
}

