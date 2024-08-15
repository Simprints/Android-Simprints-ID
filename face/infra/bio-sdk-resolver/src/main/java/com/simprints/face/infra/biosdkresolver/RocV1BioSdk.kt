package com.simprints.face.infra.biosdkresolver

import com.simprints.face.infra.rocv1.detection.RocV1Detector
import com.simprints.face.infra.rocv1.initialization.RocV1Initializer
import com.simprints.face.infra.rocv1.matching.RocV1Matcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RocV1BioSdk @Inject constructor(
    override val initializer: RocV1Initializer,
    override val detector: RocV1Detector,
    override val matcher: RocV1Matcher,
) : FaceBioSDK {
    override val version: String = "1.23"
}
