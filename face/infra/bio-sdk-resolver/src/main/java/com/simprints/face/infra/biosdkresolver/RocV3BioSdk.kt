package com.simprints.face.infra.biosdkresolver

import com.simprints.face.infra.rocv3.detection.RocV3Detector
import com.simprints.face.infra.rocv3.initialization.RocV3Initializer
import com.simprints.face.infra.rocv3.matching.RocV3Matcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RocV3BioSdk @Inject constructor(
    override val initializer: RocV3Initializer,
    override val detector: RocV3Detector,
    override val matcher: RocV3Matcher,
) : FaceBioSDK {
    override val version: String = "3.1"
}
