package com.simprints.face.infra.biosdkresolver

import javax.inject.Inject

/**
 * At the moment this is just a wrapper to make it simpler to stub it in different source sets.
 * In future we could actually need custom resolution logic.
 */
internal class ResolveSimFaceVersionUseCase @Inject constructor(
    private val simFaceBioSdk: SimFaceBioSdk,
) {
    operator fun invoke(): FaceBioSDK = simFaceBioSdk
}
