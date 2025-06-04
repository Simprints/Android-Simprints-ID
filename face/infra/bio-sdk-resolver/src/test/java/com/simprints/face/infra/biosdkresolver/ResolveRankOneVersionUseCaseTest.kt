package com.simprints.face.infra.biosdkresolver

import com.google.common.truth.*
import com.simprints.infra.config.store.ConfigRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ResolveRankOneVersionUseCaseTest {
    @MockK
    private lateinit var configRepository: ConfigRepository
    private lateinit var rocV1BioSdk: RocV1BioSdk
    private lateinit var rocV3BioSdk: RocV3BioSdk

    private lateinit var resolveTheVersionUseCase: ResolveRankOneVersionUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        rocV1BioSdk = RocV1BioSdk(mockk(), mockk())
        rocV3BioSdk = RocV3BioSdk(mockk(), mockk())

        resolveTheVersionUseCase = ResolveRankOneVersionUseCase(configRepository, rocV1BioSdk, rocV3BioSdk)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throw exception when RankOne version is null`() = runTest {
        // Given
        coEvery {
            configRepository
                .getProjectConfiguration()
                .face
                ?.rankOne
                ?.version
        } returns null

        // When
        resolveTheVersionUseCase.invoke()

        // Then: Expect IllegalArgumentException to be thrown
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throw exception when RankOne version is empty`() = runTest {
        // Given
        coEvery {
            configRepository
                .getProjectConfiguration()
                .face
                ?.rankOne
                ?.version
        } returns ""

        // When
        resolveTheVersionUseCase.invoke()

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
        val result = resolveTheVersionUseCase.invoke()

        // Then
        Truth.assertThat(result).isEqualTo(rocV3BioSdk)
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
        val result = resolveTheVersionUseCase.invoke()

        // Then
        Truth.assertThat(result).isEqualTo(rocV1BioSdk)
    }
}
