package com.simprints.face.infra.biosdkresolver

import com.simprints.biometrics.simface.SimFace
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.face.infra.basebiosdk.matching.FaceSample
import com.simprints.face.infra.simface.detection.SimFaceDetector
import com.simprints.face.infra.simface.initialization.SimFaceInitializer
import com.simprints.face.infra.simface.matching.SimFaceMatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimFaceBioSdk @Inject constructor(
    override val initializer: SimFaceInitializer,
    override val detector: SimFaceDetector,
    private val simFace: SimFace,
) : FaceBioSDK {
    override val version: String = "1"
    override val templateFormat: String = simFace.getTemplateVersion()
    override val matcherName: String = "SIM_FACE"

    override fun createMatcher(probeSamples: List<FaceSample>): FaceMatcher = SimFaceMatcher(simFace, probeSamples)
}
