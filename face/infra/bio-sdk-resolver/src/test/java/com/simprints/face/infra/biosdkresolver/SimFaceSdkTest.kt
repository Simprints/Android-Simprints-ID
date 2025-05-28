package com.simprints.face.infra.biosdkresolver

import com.google.common.truth.*
import io.mockk.*
import org.junit.Test

class SimFaceSdkTest {
    private lateinit var bioSdk: SimFaceBioSdk

    @Test
    fun createMatcher() {
        bioSdk = SimFaceBioSdk(mockk(), mockk(), mockk())

        val matcher = bioSdk.createMatcher(emptyList())

        Truth.assertThat(matcher).isNotNull()
    }
}
