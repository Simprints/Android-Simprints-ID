package com.simprints.face.infra.biosdkresolver

import com.simprints.infra.config.store.models.ModalitySdkType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResolveFaceBioSdkUseCase @Inject internal constructor(
    private val resolveRankOneVersionUseCase: ResolveRankOneVersionUseCase,
    private val resolveSimFaceVersionUseCase: ResolveSimFaceVersionUseCase,
) {
    suspend operator fun invoke(bioSdk: ModalitySdkType): FaceBioSDK = when (bioSdk) {
        ModalitySdkType.SIM_FACE -> resolveSimFaceVersionUseCase()
        ModalitySdkType.RANK_ONE -> resolveRankOneVersionUseCase()
        else -> error("Unsupported bio SDK $bioSdk")
    }
}
