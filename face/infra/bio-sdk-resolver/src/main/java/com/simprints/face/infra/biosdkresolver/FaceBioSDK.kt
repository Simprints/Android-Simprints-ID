package com.simprints.face.infra.biosdkresolver

import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.face.infra.basebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher

interface FaceBioSDK {
    val initializer: FaceBioSdkInitializer
    val detector: FaceDetector
    val matcher: FaceMatcher
    val version: String
}
