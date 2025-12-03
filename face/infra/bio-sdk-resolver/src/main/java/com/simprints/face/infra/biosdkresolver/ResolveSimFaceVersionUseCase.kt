package com.simprints.face.infra.biosdkresolver

import javax.inject.Inject

internal class ResolveSimFaceVersionUseCase @Inject constructor(
    private val simFaceBioSdk: SimFaceBioSdk,
) {
    operator fun invoke(): FaceBioSDK = simFaceBioSdk
}
