package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.event.domain.models.CandidateReadEvent.CandidateReadPayload
import com.simprints.id.data.db.event.domain.models.CandidateReadEvent.CandidateReadPayload.LocalResult
import com.simprints.id.data.db.event.domain.models.CandidateReadEvent.CandidateReadPayload.RemoteResult
import com.simprints.id.data.db.event.domain.models.CandidateReadEvent.CandidateReadPayload.RemoteResult.NOT_FOUND
import com.simprints.id.data.db.event.domain.models.CandidateReadEvent.Companion.EVENT_VERSION

import com.simprints.id.data.db.event.domain.models.EventType.CANDIDATE_READ
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test

class CandidateReadEventTest {

    @Test
    fun create_CandidateReadEvent() {
        val labels = EventLabels(sessionId = SOME_GUID1)
        val event = CandidateReadEvent(CREATED_AT, ENDED_AT, SOME_GUID1, LocalResult.NOT_FOUND, NOT_FOUND, labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(CANDIDATE_READ)
        with(event.payload as CandidateReadPayload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CANDIDATE_READ)
            assertThat(candidateId).isEqualTo(SOME_GUID1)
            assertThat(localResult).isEqualTo(LocalResult.NOT_FOUND)
            assertThat(remoteResult).isEqualTo(RemoteResult.NOT_FOUND)
        }
    }
}
