package com.simprints.face.infra.biosdkresolver

import com.simprints.infra.facenetwrapper.detection.FaceNetDetector
import com.simprints.infra.facenetwrapper.initialization.FaceNetInitializer
import com.simprints.infra.facenetwrapper.matching.FaceNetMatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FaceNetBioSdk @Inject constructor(
    override val initializer: FaceNetInitializer,
    override val detector: FaceNetDetector,
    override val matcher: FaceNetMatcher,
) : FaceBioSDK {
    override val version: String = "1.0"
}
