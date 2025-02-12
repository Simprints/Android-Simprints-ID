package com.simprints.infra.eventsync.event.remote.models

import com.google.common.truth.Truth
import com.simprints.infra.config.store.models.TokenKeyType
import io.mockk.mockk
import org.junit.Test

class ApiBiometricReferenceCreationPayloadTest {
    @Test
    fun `when getTokenizedFieldJsonPath is invoked, null is returned`() {
        val payload = ApiBiometricReferenceCreationPayload(domainPayload = mockk(relaxed = true))
        TokenKeyType.entries.forEach {
            Truth.assertThat(payload.getTokenizedFieldJsonPath(it)).isNull()
        }
    }
}
