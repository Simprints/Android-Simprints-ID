package com.simprints.face.infra.biosdkresolver

import com.simprints.biometrics.simface.SimFace
import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
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
    override fun version(): String = "1"

    override fun templateFormat(): String = simFace.getTemplateVersion()

    override fun matcherName(): String = "SIM_FACE"

    override fun createMatcher(probeReference: BiometricReferenceCapture): FaceMatcher = SimFaceMatcher(simFace, probeReference)
}
