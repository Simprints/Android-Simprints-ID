package com.simprints.face.infra.biosdkresolver

import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
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
    override fun version(): String = "1.23"

    override fun templateFormat(): String = RANK_ONE_TEMPLATE_FORMAT_1_23

    override fun matcherName(): String = "RANK_ONE"

    override fun createMatcher(probeReference: BiometricReferenceCapture): FaceMatcher = RocV1Matcher(probeReference)
}
