package com.simprints.fingerprint.infra.biosdk

import com.google.common.truth.Truth
import com.simprints.infra.config.store.models.ModalitySdkType
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ResolveBioSdkWrapperUseCaseTest {
    private lateinit var bioSdkResolverUseCase: ResolveBioSdkWrapperUseCase

    @MockK
    private lateinit var simprintsBioSdkWrapper: BioSdkWrapper

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        bioSdkResolverUseCase = ResolveBioSdkWrapperUseCase(simprintsBioSdkWrapper)
    }

    @Test
    fun `test  with simprints sdk`() = runTest {
        val result = bioSdkResolverUseCase(ModalitySdkType.SECUGEN_SIM_MATCHER)
        Truth.assertThat(simprintsBioSdkWrapper).isEqualTo(result)
    }

    @Test(expected = IllegalStateException::class)
    fun `test  with nec sdk`() = runTest {
        val result = bioSdkResolverUseCase(ModalitySdkType.NEC)
        // should throw IllegalStateException
    }
}
