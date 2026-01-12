package com.simprints.infra.eventsync.event.remote.models.samples

import com.google.common.truth.Truth.*
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.eventsync.event.remote.models.ApiEventSampleUpSyncRequestPayload
import io.mockk.*
import kotlin.test.Test

class ApiEventSampleUpSyncRequestPayloadTest {
    @Test
    fun `when getTokenizedFieldJsonPath is invoked, null is returned`() {
        val payload = ApiEventSampleUpSyncRequestPayload(domainPayload = mockk(relaxed = true))
        TokenKeyType.entries.forEach {
            assertThat(payload.getTokenizedFieldJsonPath(it)).isNull()
        }
    }
}
