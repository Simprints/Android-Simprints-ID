package com.simprints.face.infra.biosdkresolver

import com.google.common.truth.*
import com.simprints.biometrics.simface.SimFace
import com.simprints.face.infra.simface.detection.SimFaceDetector
import com.simprints.face.infra.simface.initialization.SimFaceInitializer
import io.mockk.*
import org.junit.Test

class SimFaceSdkTest {
    private lateinit var bioSdk: SimFaceBioSdk

    @Test
    fun createMatcher() {
        bioSdk = SimFaceBioSdk(
            initializer = mockk<SimFaceInitializer>(),
            detector = mockk<SimFaceDetector>(),
            simFace = mockk<SimFace>(relaxed = true),
        )

        val matcher = bioSdk.createMatcher(emptyList())

        Truth.assertThat(matcher).isNotNull()
    }
}
