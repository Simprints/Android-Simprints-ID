package com.simprints.infra.events.event.domain.models

import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.events.event.domain.models.EventType.ONE_TO_ONE_MATCH
import com.simprints.infra.events.event.domain.models.OneToOneMatchEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.event.domain.models.OneToOneMatchEvent.OneToOneMatchPayload.OneToOneMatchPayloadV3
import com.simprints.infra.events.event.domain.models.OneToOneMatchEvent.OneToOneMatchPayload.OneToOneMatchPayloadV4
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.ENDED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import org.junit.Test

class OneToOneMatchEventTest {
    @Test
    fun create_OneToOneMatchEvent() {
        val resultArg = MatchEntry(GUID1, 0F)
        val event = OneToOneMatchEvent(
            CREATED_AT,
            ENDED_AT,
            GUID1,
            "MATCHER_NAME",
            resultArg,
            FingerComparisonStrategy.SAME_FINGER,
            "referenceId",
        )

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(ONE_TO_ONE_MATCH)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(matcher).isEqualTo("MATCHER_NAME")
            assertThat(fingerComparisonStrategy)
                .isEqualTo(FingerComparisonStrategy.SAME_FINGER)
            assertThat(type).isEqualTo(ONE_TO_ONE_MATCH)
            assertThat(result).isEqualTo(resultArg)
            assertThat((this as OneToOneMatchPayloadV4).probeBiometricReferenceId).isEqualTo("referenceId")
        }
    }

    @Test
    fun shouldParse_v3Event_successfully() {
        val actualEvent = JsonHelper.fromJson(oldApiJsonEventString, object : TypeReference<Event>() {})

        assertThat(actualEvent.id).isEqualTo("3afb1b9e-b263-4073-b773-6e1dac20d72f")
        assertThat(actualEvent.payload.eventVersion).isEqualTo(3)
        assertThat(actualEvent.payload).isInstanceOf(OneToOneMatchPayloadV3::class.java)
    }

    @Test
    fun shouldParse_v4Event_successfully() {
        val actualEvent = JsonHelper.fromJson(newApiJsonEventString, object : TypeReference<Event>() {})

        assertThat(actualEvent.id).isEqualTo("3afb1b9e-b263-4073-b773-6e1dac20d72f")
        assertThat(actualEvent.payload.eventVersion).isEqualTo(4)
        assertThat(actualEvent.payload).isInstanceOf(OneToOneMatchPayloadV4::class.java)
        assertThat((actualEvent.payload as OneToOneMatchPayloadV4).probeBiometricReferenceId).isEqualTo("referenceId")
    }

    private val oldApiJsonEventString =
        """
        {
            "id":"3afb1b9e-b263-4073-b773-6e1dac20d72f",
            "scopeId":"6dcb3810-4789-4149-8fea-473ffb520958",
            "payload":{
                "createdAt":{"ms":1234},
                "eventVersion":3,
                "candidateId":"3afb1b9e-b263-4073-b773-6e1dac20d72f",
                "matcher":"SIM_AFIS",
                "result":{"candidateId":"3afb1b9e-b263-4073-b773-6e1dac20d72f","score":1.0},
                "fingerComparisonStrategy":"SAME_FINGER",
                "type":"ONE_TO_ONE_MATCH",
                "endedAt":{"ms":4567}
            },
            "type":"ONE_TO_ONE_MATCH"
        }
        """.trimIndent()

    private val newApiJsonEventString =
        """
        {
            "id":"3afb1b9e-b263-4073-b773-6e1dac20d72f",
            "scopeId":"6dcb3810-4789-4149-8fea-473ffb520958",
            "payload":{
                "createdAt":{"ms":1234},
                "eventVersion":4,
                "candidateId":"3afb1b9e-b263-4073-b773-6e1dac20d72f",
                "matcher":"SIM_AFIS",
                "result":{"candidateId":"3afb1b9e-b263-4073-b773-6e1dac20d72f","score":1.0},
                "fingerComparisonStrategy":"SAME_FINGER",
                "type":"ONE_TO_ONE_MATCH",
                "endedAt":{"ms":4567},
                "probeBiometricReferenceId": "referenceId"
            },
            "type":"ONE_TO_ONE_MATCH"
        }
        """.trimIndent()
}
