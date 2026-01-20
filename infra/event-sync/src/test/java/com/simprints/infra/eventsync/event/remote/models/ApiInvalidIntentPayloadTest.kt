package com.simprints.infra.eventsync.event.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.InvalidIntentEvent
import io.mockk.mockk
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test

class ApiInvalidIntentPayloadTest {
    @Test
    fun `when getTokenizedFieldJsonPath is invoked, null is returned`() {
        val payload = ApiInvalidIntentPayload(domainPayload = mockk(relaxed = true))
        TokenKeyType.values().forEach {
            assertThat(payload.getTokenizedFieldJsonPath(it)).isNull()
        }
    }

    @Test
    fun `converts extras jsonElements to string`() {
        val domainPayload = InvalidIntentEvent.InvalidIntentPayload(
            createdAt = Timestamp(123L),
            eventVersion = 1,
            action = "Enrol",
            extras = mapOf("key" to JsonPrimitive("value"), "key2" to JsonPrimitive(123)),
        )
        val apiInvalidIntentPayload = ApiInvalidIntentPayload(domainPayload = domainPayload)
        assertThat(apiInvalidIntentPayload.extras).isEqualTo(mapOf("key" to "value", "key2" to "123"))
    }
}
