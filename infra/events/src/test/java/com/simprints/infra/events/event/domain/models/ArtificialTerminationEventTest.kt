package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth
import com.simprints.infra.events.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.infra.events.event.domain.models.EventType.ARTIFICIAL_TERMINATION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import org.junit.Test

class ArtificialTerminationEventTest {

    @Test
    fun create_ArtificialTerminationEvent() {
        val event = ArtificialTerminationEvent(CREATED_AT, NEW_SESSION)
        Truth.assertThat(event.id).isNotNull()
        Truth.assertThat(event.type).isEqualTo(ARTIFICIAL_TERMINATION)
        with(event.payload) {
            Truth.assertThat(createdAt).isEqualTo(CREATED_AT)
            Truth.assertThat(endedAt).isNull()
            Truth.assertThat(eventVersion).isEqualTo(ArtificialTerminationEvent.EVENT_VERSION)
            Truth.assertThat(type).isEqualTo(ARTIFICIAL_TERMINATION)
            Truth.assertThat(reason).isEqualTo(NEW_SESSION)
        }
    }
}
