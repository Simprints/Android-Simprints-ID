package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat

import com.simprints.id.data.db.event.domain.models.EventType.ONE_TO_MANY_MATCH
import com.simprints.id.data.db.event.domain.models.Matcher.RANK_ONE
import com.simprints.id.data.db.event.domain.models.OneToManyMatchEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPool
import com.simprints.id.data.db.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.PROJECT
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test

class OneToManyMatchEventTest {

    @Test
    fun create_OneToManyMatchEvent() {
        val labels = EventLabels(sessionId = SOME_GUID1)
        val poolArg = MatchPool(PROJECT, 100)
        val resultArg = listOf(MatchEntry(SOME_GUID1, 0F))
        val event = OneToManyMatchEvent(CREATED_AT, ENDED_AT, poolArg, RANK_ONE, resultArg, labels)

        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(ONE_TO_MANY_MATCH)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(ONE_TO_MANY_MATCH)
            assertThat(matcher).isEqualTo(RANK_ONE)
            assertThat(pool).isEqualTo(poolArg)
            assertThat(result).isEqualTo(resultArg)
        }
    }
}
