package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.id.sampledata.DefaultTestConstants.CREATED_AT
import com.simprints.id.sampledata.DefaultTestConstants.ENDED_AT
import com.simprints.id.data.db.event.domain.models.ConsentEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.ConsentEvent.ConsentPayload
import com.simprints.id.data.db.event.domain.models.ConsentEvent.ConsentPayload.Result.ACCEPTED
import com.simprints.id.data.db.event.domain.models.ConsentEvent.ConsentPayload.Type.INDIVIDUAL
import com.simprints.id.data.db.event.domain.models.EventType.CONSENT
import com.simprints.id.sampledata.DefaultTestConstants.GUID1
import org.junit.Test

class ConsentEventTest {

    @Test
    fun create_ConsentEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val event = ConsentEvent(CREATED_AT, ENDED_AT, INDIVIDUAL, ACCEPTED, labels)
        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(CONSENT)
        with(event.payload as ConsentPayload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(CONSENT)
            assertThat(consentType).isEqualTo(INDIVIDUAL)
            assertThat(result).isEqualTo(ACCEPTED)
        }
    }
}
