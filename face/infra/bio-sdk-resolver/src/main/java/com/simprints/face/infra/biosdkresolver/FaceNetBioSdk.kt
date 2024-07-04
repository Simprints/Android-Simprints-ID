package com.simprints.face.infra.biosdkresolver

import com.simprints.infra.mlkitwrapper.detection.MlKitDetector
import com.simprints.infra.mlkitwrapper.initialization.FaceNetInitializer
import com.simprints.infra.mlkitwrapper.matching.MlKitMatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FaceNetBioSdk @Inject constructor(
    override val initializer: FaceNetInitializer,
    override val detector: MlKitDetector,
    override val matcher: MlKitMatcher,
) : FaceBioSDK {
    override val version: String = "1.0"
}
