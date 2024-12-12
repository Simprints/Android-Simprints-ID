package com.simprints.infra.eventsync.event.remote.models

import com.google.common.truth.Truth
import com.simprints.infra.config.store.models.TokenKeyType
import io.mockk.mockk
import org.junit.Test

class ApiAuthorizationPayloadTest {
    @Test
    fun `when getTokenizedFieldJsonPath is invoked, correct fields are returned`() {
        val payload = ApiAuthorizationPayload(domainPayload = mockk(relaxed = true))
        TokenKeyType.values().forEach {
            val result = payload.getTokenizedFieldJsonPath(it)
            when (it) {
                TokenKeyType.AttendantId -> Truth.assertThat(result).isEqualTo("userInfo.userId")
                else -> Truth.assertThat(result).isNull()
            }
        }
    }
}
