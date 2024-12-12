package com.simprints.infra.eventsync.event.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.TokenKeyType
import io.mockk.mockk
import org.junit.Test

class ApiOneToManyMatchPayloadTest {
    @Test
    fun `when getTokenizedFieldJsonPath is invoked, null is returned`() {
        val payload = ApiOneToManyMatchPayload(
            startTime = ApiTimestamp(0L, false, 0L),
            endTime = ApiTimestamp(0L, false, 0L),
            pool = mockk(),
            matcher = "",
            result = null,
        )
        TokenKeyType.values().forEach {
            assertThat(payload.getTokenizedFieldJsonPath(it)).isNull()
        }
    }
}
