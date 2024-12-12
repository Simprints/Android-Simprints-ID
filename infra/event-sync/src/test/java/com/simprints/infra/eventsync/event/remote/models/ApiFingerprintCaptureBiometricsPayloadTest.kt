package com.simprints.infra.eventsync.event.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.TokenKeyType
import io.mockk.mockk
import org.junit.Test

class ApiFingerprintCaptureBiometricsPayloadTest {
    @Test
    fun `when getTokenizedFieldJsonPath is invoked, null is returned`() {
        val payload = ApiFingerprintCaptureBiometricsPayload(domainPayload = mockk(relaxed = true))
        TokenKeyType.values().forEach {
            assertThat(payload.getTokenizedFieldJsonPath(it)).isNull()
        }
    }
}
