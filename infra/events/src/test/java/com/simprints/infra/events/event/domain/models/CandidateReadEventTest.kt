package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.CandidateReadEvent.CandidateReadPayload.LocalResult
import com.simprints.infra.events.event.domain.models.CandidateReadEvent.CandidateReadPayload.RemoteResult
import com.simprints.infra.events.event.domain.models.CandidateReadEvent.CandidateReadPayload.RemoteResult.NOT_FOUND
import com.simprints.infra.events.event.domain.models.CandidateReadEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.event.domain.models.EventType.CANDIDATE_READ
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.ENDED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import org.junit.Test

class CandidateReadEventTest {
    @Test
    fun create_CandidateReadEvent() {
        val event = CandidateReadEvent(CREATED_AT, ENDED_AT, GUID1, LocalResult.NOT_FOUND, NOT_FOUND)

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(CANDIDATE_READ)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CANDIDATE_READ)
            assertThat(candidateId).isEqualTo(GUID1)
            assertThat(localResult).isEqualTo(LocalResult.NOT_FOUND)
            assertThat(remoteResult).isEqualTo(RemoteResult.NOT_FOUND)
        }
    }
}
