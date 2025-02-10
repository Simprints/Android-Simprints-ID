package com.simprints.infra.events.event.domain.models

import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.events.event.domain.models.EventType.ONE_TO_MANY_MATCH
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPool
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.PROJECT
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.OneToManyMatchPayloadV2
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.OneToManyMatchPayloadV3
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.ENDED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import org.junit.Test

class OneToManyMatchEventTest {
    @Test
    fun create_OneToManyMatchEvent() {
        val poolArg = MatchPool(PROJECT, 100)
        val resultArg = listOf(MatchEntry(GUID1, 0F))
        val event = OneToManyMatchEvent(CREATED_AT, ENDED_AT, poolArg, "MATCHER_NAME", resultArg, "referenceId")

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
            assertThat((this as OneToManyMatchPayloadV3).probeBiometricReferenceId).isEqualTo("referenceId")
        }
    }

    @Test
    fun shouldParse_v2Event_successfully() {
        val actualEvent = JsonHelper.fromJson(oldApiJsonEventString, object : TypeReference<Event>() {})

        assertThat(actualEvent.id).isEqualTo("3afb1b9e-b263-4073-b773-6e1dac20d72f")
        assertThat(actualEvent.payload.eventVersion).isEqualTo(2)
        assertThat(actualEvent.payload).isInstanceOf(OneToManyMatchPayloadV2::class.java)
    }

    @Test
    fun shouldParse_v3Event_successfully() {
        val actualEvent = JsonHelper.fromJson(newApiJsonEventString, object : TypeReference<Event>() {})

        assertThat(actualEvent.id).isEqualTo("3afb1b9e-b263-4073-b773-6e1dac20d72f")
        assertThat(actualEvent.payload.eventVersion).isEqualTo(3)
        assertThat(actualEvent.payload).isInstanceOf(OneToManyMatchPayloadV3::class.java)
        assertThat((actualEvent.payload as OneToManyMatchPayloadV3).probeBiometricReferenceId).isEqualTo("referenceId")
    }

    private val oldApiJsonEventString =
        """
        {
            "id": "3afb1b9e-b263-4073-b773-6e1dac20d72f",
            "scopeId": "6dcb3810-4789-4149-8fea-473ffb520958",
            "payload": {
                "createdAt": {"ms": 1234},
                "eventVersion": 2,
                "pool": {
                  "type": "PROJECT",
                  "count": 1040
                },
                "matcher": "SIM_AFIS",
                "results": [],
                "type": "ONE_TO_MANY_MATCH",
                "endedAt": {"ms": 4567}
            },
            "type": "ONE_TO_MANY_MATCH"
        }
        """.trimIndent()

    private val newApiJsonEventString =
        """
        {
            "id": "3afb1b9e-b263-4073-b773-6e1dac20d72f",
            "scopeId": "6dcb3810-4789-4149-8fea-473ffb520958",
            "payload": {
                "createdAt": {"ms": 1234},
                "eventVersion": 3,
                "pool": {
                  "type": "PROJECT",
                  "count": 1040
                },
                "matcher": "SIM_AFIS",
                "results": [],
                "probeBiometricReferenceId": "referenceId",
                "type": "ONE_TO_MANY_MATCH",
                "endedAt": {"ms": 1234}
            },
            "type": "ONE_TO_MANY_MATCH"
        }
        """.trimIndent()
}
