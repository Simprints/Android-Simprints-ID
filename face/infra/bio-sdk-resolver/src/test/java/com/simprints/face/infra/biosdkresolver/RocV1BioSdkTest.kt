package com.simprints.face.infra.biosdkresolver

import com.google.common.truth.Truth.*
import io.mockk.*
import org.junit.Test

class RocV1BioSdkTest {
    private lateinit var rocV1BioSdk: RocV1BioSdk

    @Test
    fun createMatcher() {
        rocV1BioSdk = RocV1BioSdk(mockk(), mockk())

        val matcher = rocV1BioSdk.createMatcher(
            mockk { every { templates } returns emptyList() },
        )

        assertThat(matcher).isNotNull()
    }
}
