package com.simprints.infra.eventsync.event.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.TokenKeyType
import io.mockk.mockk
import org.junit.Test

class ApiEnrolmentPayloadV2Test {
    @Test
    fun `when getTokenizedFieldJsonPath is invoked, correct fields are returned`() {
        val payload = ApiEnrolmentPayloadV2(domainPayload = mockk(relaxed = true))
        TokenKeyType.values().forEach {
            val result = payload.getTokenizedFieldJsonPath(it)
            when (it) {
                TokenKeyType.AttendantId -> assertThat(result).isEqualTo("attendantId")
                TokenKeyType.ModuleId -> assertThat(result).isEqualTo("moduleId")
                else -> assertThat(result).isNull()
            }
        }
    }
}
