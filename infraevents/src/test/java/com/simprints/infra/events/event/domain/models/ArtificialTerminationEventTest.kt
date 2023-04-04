package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth
import com.simprints.infra.events.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.infra.events.event.domain.models.EventType.ARTIFICIAL_TERMINATION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_ENDED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import org.junit.Test

class ArtificialTerminationEventTest {

    @Test
    fun create_ArtificialTerminationEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val event = ArtificialTerminationEvent(CREATED_AT, NEW_SESSION, labels)
        Truth.assertThat(event.id).isNotNull()
        Truth.assertThat(event.labels).isEqualTo(labels)
        Truth.assertThat(event.type).isEqualTo(ARTIFICIAL_TERMINATION)
        with(event.payload) {
            Truth.assertThat(createdAt).isEqualTo(CREATED_AT)
            Truth.assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            Truth.assertThat(eventVersion).isEqualTo(ArtificialTerminationEvent.EVENT_VERSION)
            Truth.assertThat(type).isEqualTo(ARTIFICIAL_TERMINATION)
            Truth.assertThat(reason).isEqualTo(NEW_SESSION)
        }
    }
}