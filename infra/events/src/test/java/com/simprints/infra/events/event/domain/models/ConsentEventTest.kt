package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.*
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.events.event.domain.models.ConsentEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.event.domain.models.ConsentEvent.ConsentPayload.Result.ACCEPTED
import com.simprints.infra.events.event.domain.models.ConsentEvent.ConsentPayload.Type.INDIVIDUAL
import com.simprints.infra.events.event.domain.models.EventType.CONSENT
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.ENDED_AT
import com.simprints.infra.serialization.SimJson
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

    @Test
    fun serialize_and_deserialize_ConsentEvent() {
        // Given
        val createdAt = Timestamp(0)
        val endedAt = Timestamp(0)
        val event = ConsentEvent(
            createdAt = createdAt,
            endTime = endedAt,
            consentType = INDIVIDUAL,
            result = ACCEPTED,
        )

        val json = SimJson.encodeToString(event)
        // When
        val decoded = SimJson.decodeFromString<ConsentEvent>(json)

        // Then
        assertThat(decoded.id).isEqualTo(event.id)
        assertThat(decoded.type).isEqualTo(event.type)
        assertThat(decoded.scopeId).isEqualTo(event.scopeId)
        assertThat(decoded.projectId).isEqualTo(event.projectId)

        // Payload assertions
        val expectedPayload = event.payload
        val decodedPayload = decoded.payload

        assertThat(decodedPayload.createdAt).isEqualTo(expectedPayload.createdAt)
        assertThat(decodedPayload.endedAt).isEqualTo(expectedPayload.endedAt)
        assertThat(decodedPayload.eventVersion).isEqualTo(expectedPayload.eventVersion)
        assertThat(decodedPayload.consentType).isEqualTo(expectedPayload.consentType)
        assertThat(decodedPayload.result).isEqualTo(expectedPayload.result)
        assertThat(decodedPayload.type).isEqualTo(expectedPayload.type)
    }
}
