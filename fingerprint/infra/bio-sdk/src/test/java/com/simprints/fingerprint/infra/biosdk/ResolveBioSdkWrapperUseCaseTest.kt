package com.simprints.fingerprint.infra.biosdk

import com.google.common.truth.Truth
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.FingerprintConfiguration
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ResolveBioSdkWrapperUseCaseTest {

    private lateinit var bioSdkResolverUseCase: ResolveBioSdkWrapperUseCase

    @MockK
    private lateinit var necBioSdkWrapper: BioSdkWrapper

    @MockK
    private lateinit var simprintsBioSdkWrapper: BioSdkWrapper

    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var fingerprintConfiguration: FingerprintConfiguration

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { configRepository.getProjectConfiguration() } returns mockk {
            every { fingerprint } returns fingerprintConfiguration
        }
        bioSdkResolverUseCase =
            ResolveBioSdkWrapperUseCase(configRepository, simprintsBioSdkWrapper, necBioSdkWrapper)
    }

    @Test
    fun `test  with simprints sdk`() = runTest {
        every {
            fingerprintConfiguration.allowedSDKs
        } returns listOf(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER)


        val result = bioSdkResolverUseCase()
        Truth.assertThat(simprintsBioSdkWrapper).isEqualTo(result)
    }

    @Test
    fun `test  with nec sdk`() = runTest {
        every {
            fingerprintConfiguration.allowedSDKs
        } returns listOf(FingerprintConfiguration.BioSdk.NEC)

        val result = bioSdkResolverUseCase()
        Truth.assertThat(necBioSdkWrapper).isEqualTo(result)
    }

    @Test
    fun `test if already initialized`() = runTest {
        every {
            fingerprintConfiguration.allowedSDKs
        } returns listOf(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER)

        val result = bioSdkResolverUseCase()
        Truth.assertThat(simprintsBioSdkWrapper).isEqualTo(result)

        val result2 = bioSdkResolverUseCase()
        Truth.assertThat(simprintsBioSdkWrapper).isEqualTo(result2)

        coVerify(exactly = 1) { fingerprintConfiguration.allowedSDKs }
    }
}
