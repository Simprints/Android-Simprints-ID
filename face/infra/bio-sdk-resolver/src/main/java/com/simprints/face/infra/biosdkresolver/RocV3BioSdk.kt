package com.simprints.face.infra.biosdkresolver

import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.face.infra.rocv3.detection.RocV3Detector
import com.simprints.face.infra.rocv3.detection.RocV3Detector.Companion.RANK_ONE_TEMPLATE_FORMAT_3_1
import com.simprints.face.infra.rocv3.initialization.RocV3Initializer
import com.simprints.face.infra.rocv3.matching.RocV3Matcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RocV3BioSdk @Inject constructor(
    override val initializer: RocV3Initializer,
    override val detector: RocV3Detector,
) : FaceBioSDK {
    override fun version(): String = "3.1"

    override fun templateFormat(): String = RANK_ONE_TEMPLATE_FORMAT_3_1

    override fun matcherName(): String = "RANK_ONE"

    override fun createMatcher(probeReference: BiometricReferenceCapture): FaceMatcher = RocV3Matcher(probeReference)
}
