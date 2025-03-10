package com.simprints.ear.infra.biosdkresolver

import com.simprints.ear.infra.basebiosdk.detection.EarDetector
import com.simprints.ear.infra.basebiosdk.initialization.EarBioSdkInitializer
import com.simprints.ear.infra.basebiosdk.matching.EarMatcher

interface EarBioSDK {
    val initializer: EarBioSdkInitializer
    val detector: EarDetector
    val matcher: EarMatcher
    val version: String
}
