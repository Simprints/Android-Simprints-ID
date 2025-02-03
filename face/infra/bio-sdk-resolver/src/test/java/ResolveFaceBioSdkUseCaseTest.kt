package com.simprints.face.infra.biosdkresolver

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.ConfigRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.lang.IllegalArgumentException

class ResolveFaceBioSdkUseCaseTest {
    private lateinit var resolveFaceBioSdkUseCase: ResolveFaceBioSdkUseCase
    private val configRepository: ConfigRepository = mockk()
    private lateinit var rocV1BioSdk: RocV1BioSdk
    private lateinit var rocV3BioSdk: RocV3BioSdk

    @Before
    fun setUp() {
        rocV1BioSdk = RocV1BioSdk(mockk(), mockk(), mockk())
        rocV3BioSdk = RocV3BioSdk(mockk(), mockk(), mockk())
        resolveFaceBioSdkUseCase = ResolveFaceBioSdkUseCase(configRepository, rocV1BioSdk, rocV3BioSdk)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throw exception when version is null`() = runTest {
        // Given
        coEvery {
            configRepository
                .getProjectConfiguration()
                .face
                ?.rankOne
                ?.version
        } returns null

        // When
        resolveFaceBioSdkUseCase.invoke()

        // Then: Expect IllegalArgumentException to be thrown
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throw exception when version is empty`() = runTest {
        // Given
        coEvery {
            configRepository
                .getProjectConfiguration()
                .face
                ?.rankOne
                ?.version
        } returns ""

        // When
        resolveFaceBioSdkUseCase.invoke()

        // Then: Expect IllegalArgumentException to be thrown
    }

    // Given that version is valid and matches ROC V3
    @Test
    fun `return ROC V3 SDK when version is valid and matches ROC V3`() = runTest {
        // Given

        coEvery {
            configRepository
                .getProjectConfiguration()
                .face
                ?.rankOne
                ?.version
        } returns rocV3BioSdk.version

        // When
        val result = resolveFaceBioSdkUseCase.invoke()

        // Then
        assertThat(result).isEqualTo(rocV3BioSdk)
    }

    // Given that version is valid and does not match ROC V3 (should match ROC V1)
    @Test
    fun `return ROC V1 SDK when version is valid and does not match ROC V3`() = runTest {
        // Given
        coEvery {
            configRepository
                .getProjectConfiguration()
                .face
                ?.rankOne
                ?.version
        } returns rocV1BioSdk.version

        // When
        val result = resolveFaceBioSdkUseCase.invoke()

        // Then
        assertThat(result).isEqualTo(rocV1BioSdk)
    }
}
