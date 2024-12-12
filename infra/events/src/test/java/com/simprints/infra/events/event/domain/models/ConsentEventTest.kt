package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.ConsentEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.event.domain.models.ConsentEvent.ConsentPayload.Result.ACCEPTED
import com.simprints.infra.events.event.domain.models.ConsentEvent.ConsentPayload.Type.INDIVIDUAL
import com.simprints.infra.events.event.domain.models.EventType.CONSENT
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.ENDED_AT
import org.junit.Test

class ConsentEventTest {
    @Test
    fun create_ConsentEvent() {
        val event = ConsentEvent(CREATED_AT, ENDED_AT, INDIVIDUAL, ACCEPTED)

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(CONSENT)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CONSENT)
            assertThat(consentType).isEqualTo(INDIVIDUAL)
            assertThat(result).isEqualTo(ACCEPTED)
        }
    }
}
