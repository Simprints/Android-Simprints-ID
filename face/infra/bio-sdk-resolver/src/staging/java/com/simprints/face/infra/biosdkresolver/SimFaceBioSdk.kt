package com.simprints.face.infra.biosdkresolver

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.face.infra.basebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.face.infra.basebiosdk.matching.FaceSample
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class interfaces in the different build types must be identical for it to work,
 * therefore we have to stub the whole class for now.
 */
@ExcludedFromGeneratedTestCoverageReports("Stubs for build types")
@Singleton
class SimFaceBioSdk @Inject constructor() : FaceBioSDK {
    override val initializer: FaceBioSdkInitializer
        get() = TODO()
    override val detector: FaceDetector
        get() = TODO()
    override val version: String
        get() = TODO()
    override val templateFormat: String
        get() = TODO()
    override val matcherName: String
        get() = TODO()

    override fun createMatcher(probeSamples: List<FaceSample>): FaceMatcher = TODO()
}
