package com.simprints.face.infra.biosdkresolver

import com.simprints.core.domain.reference.BiometricReferenceCapture
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.face.infra.basebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher

interface FaceBioSDK {
    val initializer: FaceBioSdkInitializer
    val detector: FaceDetector

    fun version(): String

    fun templateFormat(): String

    fun matcherName(): String

    fun createMatcher(probeReference: BiometricReferenceCapture): FaceMatcher
}
