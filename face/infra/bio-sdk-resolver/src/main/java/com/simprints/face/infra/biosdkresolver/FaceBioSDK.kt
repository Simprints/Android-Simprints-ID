package com.simprints.face.infra.biosdkresolver

import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.face.infra.basebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.face.infra.basebiosdk.matching.FaceSample

interface FaceBioSDK {
    val initializer: FaceBioSdkInitializer
    val detector: FaceDetector

    val version: String
    val templateFormat: String
    val matcherName: String

    fun createMatcher(probeSamples: List<FaceSample>): FaceMatcher
}
