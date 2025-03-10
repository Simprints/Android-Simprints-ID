package com.simprints.ear.infra.biosdkresolver

import com.simprints.ear.infra.earsdk.EarSimFaceDetector
import com.simprints.ear.infra.earsdk.EarSimFaceInitializer
import com.simprints.ear.infra.earsdk.EarSimFaceMatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EarSimFaceBioSdk @Inject constructor(
    override val initializer: EarSimFaceInitializer,
    override val detector: EarSimFaceDetector,
    override val matcher: EarSimFaceMatcher,
) : EarBioSDK {
    override val version: String = "1"
}
