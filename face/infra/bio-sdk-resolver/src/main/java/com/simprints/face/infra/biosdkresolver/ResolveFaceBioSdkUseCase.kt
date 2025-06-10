package com.simprints.face.infra.biosdkresolver

import com.simprints.infra.config.store.models.FaceConfiguration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResolveFaceBioSdkUseCase @Inject internal constructor(
    private val resolveRankOneVersionUseCase: ResolveRankOneVersionUseCase,
    private val resolveSimFaceVersionUseCase: ResolveSimFaceVersionUseCase,
) {
    suspend operator fun invoke(bioSdk: FaceConfiguration.BioSdk): FaceBioSDK = when (bioSdk) {
        FaceConfiguration.BioSdk.SIM_FACE -> resolveSimFaceVersionUseCase()
        FaceConfiguration.BioSdk.RANK_ONE -> resolveRankOneVersionUseCase()
    }
}
