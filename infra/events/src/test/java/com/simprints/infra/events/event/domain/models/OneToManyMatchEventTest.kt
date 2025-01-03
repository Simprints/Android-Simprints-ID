package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.EventType.ONE_TO_MANY_MATCH
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPool
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.PROJECT
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.ENDED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import org.junit.Test

class OneToManyMatchEventTest {
    @Test
    fun create_OneToManyMatchEvent() {
        val poolArg = MatchPool(PROJECT, 100)
        val resultArg = listOf(MatchEntry(GUID1, 0F))
        val event = OneToManyMatchEvent(CREATED_AT, ENDED_AT, poolArg, "MATCHER_NAME", resultArg)

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(ONE_TO_MANY_MATCH)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(ONE_TO_MANY_MATCH)
            assertThat(matcher).isEqualTo("MATCHER_NAME")
            assertThat(pool).isEqualTo(poolArg)
            assertThat(result).isEqualTo(resultArg)
        }
    }
}
