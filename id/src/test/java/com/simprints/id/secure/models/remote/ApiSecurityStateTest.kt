package com.simprints.id.secure.models.remote

import com.google.common.truth.Truth.assertThat
import com.simprints.id.secure.models.SecurityState
import org.junit.Test

class ApiSecurityStateTest {

    @Test
    fun `should map correctly the Status enums`() {
        val mapping = mapOf(
            ApiSecurityState.Status.RUNNING to SecurityState.Status.RUNNING,
            ApiSecurityState.Status.COMPROMISED to SecurityState.Status.COMPROMISED,
            ApiSecurityState.Status.PROJECT_ENDED to SecurityState.Status.PROJECT_ENDED,
        )

        mapping.forEach {
            assertThat(it.key.fromApiToDomain()).isEqualTo(it.value)
        }
    }
}
