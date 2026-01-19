package com.simprints.face.infra.biosdkresolver

import com.google.common.truth.Truth.*
import com.simprints.infra.config.store.models.ModalitySdkType
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ResolveFaceBioSdkUseCaseTest {
    @MockK
    private lateinit var resolveRankOneVersionUseCase: ResolveRankOneVersionUseCase

    @MockK
    private lateinit var rocBioSdk: RocV1BioSdk

    @MockK
    private lateinit var resolveSimFaceVersionUseCase: ResolveSimFaceVersionUseCase

    @MockK
    private lateinit var simFaceBioSdk: SimFaceBioSdk

    private lateinit var resolveFaceBioSdkUseCase: ResolveFaceBioSdkUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { resolveRankOneVersionUseCase() } returns rocBioSdk
        coEvery { resolveSimFaceVersionUseCase() } returns simFaceBioSdk

        resolveFaceBioSdkUseCase = ResolveFaceBioSdkUseCase(
            resolveRankOneVersionUseCase,
            resolveSimFaceVersionUseCase,
        )
    }

    @Test
    fun `return SimFace SDK when requested`() = runTest {
        assertThat(resolveFaceBioSdkUseCase.invoke(ModalitySdkType.SIM_FACE)).isEqualTo(simFaceBioSdk)
    }

    @Test
    fun `return RankOne SDK when requested`() = runTest {
        assertThat(resolveFaceBioSdkUseCase.invoke(ModalitySdkType.RANK_ONE)).isEqualTo(rocBioSdk)
    }
}
