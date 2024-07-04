package com.simprints.face.infra.biosdkresolver

import com.simprints.infra.mlkitwrapper.detection.MlKitDetector
import com.simprints.infra.mlkitwrapper.initialization.EdgeFaceInitializer
import com.simprints.infra.mlkitwrapper.matching.MlKitMatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EdgeFaceBioSdk @Inject constructor(
    override val initializer: EdgeFaceInitializer,
    override val detector: MlKitDetector,
    override val matcher: MlKitMatcher,
) : FaceBioSDK {
    override val version: String = "1.0"
}
