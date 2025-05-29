package com.simprints.face.infra.biosdkresolver

import javax.inject.Inject

/**
 * Class interfaces in the different build types must be identical for it to work,
 * therefore we have to stub the whole class for now.
 */
@ExcludedFromGeneratedTestCoverageReports("Stubs for build types")
internal class ResolveSimFaceVersionUseCase @Inject constructor(
    private val simFaceBioSdk: SimFaceBioSdk,
) {
    operator fun invoke(): FaceBioSDK = TODO("SimFace is not available in release build")
}
