package com.simprints.eventsystem.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.id.sampledata.SampleDefaults.GUID1
import com.simprints.id.sampledata.SampleDefaults.CREATED_AT
import com.simprints.id.sampledata.SampleDefaults.ENDED_AT
import com.simprints.eventsystem.event.domain.models.CandidateReadEvent.CandidateReadPayload.LocalResult
import com.simprints.eventsystem.event.domain.models.CandidateReadEvent.CandidateReadPayload.RemoteResult
import com.simprints.eventsystem.event.domain.models.CandidateReadEvent.CandidateReadPayload.RemoteResult.NOT_FOUND
import com.simprints.eventsystem.event.domain.models.CandidateReadEvent.Companion.EVENT_VERSION
import com.simprints.eventsystem.event.domain.models.EventType.CANDIDATE_READ
import org.junit.Test

class CandidateReadEventTest {

    @Test
    fun create_CandidateReadEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val event = CandidateReadEvent(CREATED_AT, ENDED_AT, GUID1, LocalResult.NOT_FOUND, NOT_FOUND, labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
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
