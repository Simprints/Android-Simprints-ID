package com.simprints.face.infra.biosdkresolver

import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.face.infra.basebiosdk.matching.FaceSample
import com.simprints.face.infra.rocv1.detection.RocV1Detector
import com.simprints.face.infra.rocv1.detection.RocV1Detector.Companion.RANK_ONE_TEMPLATE_FORMAT_1_23
import com.simprints.face.infra.rocv1.initialization.RocV1Initializer
import com.simprints.face.infra.rocv1.matching.RocV1Matcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RocV1BioSdk @Inject constructor(
    override val initializer: RocV1Initializer,
    override val detector: RocV1Detector,
) : FaceBioSDK {
    override val version: String = "1.23"
    override val templateFormat: String = RANK_ONE_TEMPLATE_FORMAT_1_23
    override val matcherName: String = "RANK_ONE"

    override fun createMatcher(probeSamples: List<FaceSample>): FaceMatcher = RocV1Matcher(probeSamples)
}
