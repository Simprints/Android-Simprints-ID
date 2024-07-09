package com.simprints.fingerprint.infra.biosdk

import com.google.common.truth.Truth
import com.simprints.infra.config.store.models.FingerprintConfiguration.BioSdk.NEC
import com.simprints.infra.config.store.models.FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ResolveBioSdkWrapperUseCaseTest {

    private lateinit var bioSdkResolverUseCase: ResolveBioSdkWrapperUseCase

    @MockK
    private lateinit var necBioSdkWrapper: BioSdkWrapper

    @MockK
    private lateinit var simprintsBioSdkWrapper: BioSdkWrapper

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        bioSdkResolverUseCase = ResolveBioSdkWrapperUseCase(simprintsBioSdkWrapper, necBioSdkWrapper)
    }

    @Test
    fun `test  with simprints sdk`() = runTest {
        val result = bioSdkResolverUseCase(SECUGEN_SIM_MATCHER)
        Truth.assertThat(simprintsBioSdkWrapper).isEqualTo(result)
    }

    @Test
    fun `test  with nec sdk`() = runTest {
        val result = bioSdkResolverUseCase(NEC)
        Truth.assertThat(necBioSdkWrapper).isEqualTo(result)
    }
}
