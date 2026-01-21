package com.simprints.infra.eventsync.event.remote.models

import com.google.common.truth.Truth.*
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.SuspiciousIntentEvent
import io.mockk.*
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test

class ApiSuspiciousIntentPayloadTest {
    @Test
    fun `when getTokenizedFieldJsonPath is invoked, null is returned`() {
        val payload = ApiSuspiciousIntentPayload(domainPayload = mockk(relaxed = true))
        TokenKeyType.entries.forEach {
            assertThat(payload.getTokenizedFieldJsonPath(it)).isNull()
        }
    }

    @Test
    fun `converts extras jsonElements to string`() {
        val domainPayload = SuspiciousIntentEvent.SuspiciousIntentPayload(
            createdAt = Timestamp(123L),
            eventVersion = 1,
            unexpectedExtras = mapOf("key" to JsonPrimitive("value"), "key2" to JsonPrimitive(123)),
        )
        val apiSuspiciousIntentPayload = ApiSuspiciousIntentPayload(domainPayload = domainPayload)
        assertThat(apiSuspiciousIntentPayload.unexpectedExtras).isEqualTo(mapOf("key" to "value", "key2" to "123"))
    }
}
