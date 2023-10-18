package com.simprints.infra.eventsync.event.remote.models

import com.google.common.truth.Truth
import com.simprints.infra.config.store.models.TokenKeyType
import io.mockk.mockk
import org.junit.Test

class ApiArtificialTerminationPayloadTest {

    @Test
    fun `when getTokenizedFieldJsonPath is invoked, null is returned`() {
        val payload = ApiArtificialTerminationPayload(domainPayload = mockk(relaxed = true))
        TokenKeyType.values().forEach {
            Truth.assertThat(payload.getTokenizedFieldJsonPath(it)).isNull()
        }
    }
}