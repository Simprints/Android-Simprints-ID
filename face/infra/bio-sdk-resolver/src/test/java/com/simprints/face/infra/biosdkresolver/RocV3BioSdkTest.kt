package com.simprints.face.infra.biosdkresolver

import com.google.common.truth.Truth.*
import io.mockk.*
import org.junit.Test

class RocV3BioSdkTest {
    private lateinit var rocV3BioSdk: RocV3BioSdk

    @Test
    fun createMatcher() {
        rocV3BioSdk = RocV3BioSdk(mockk(), mockk())

        val matcher = rocV3BioSdk.createMatcher(
            mockk { every { templates } returns emptyList() },
        )

        assertThat(matcher).isNotNull()
    }
}
