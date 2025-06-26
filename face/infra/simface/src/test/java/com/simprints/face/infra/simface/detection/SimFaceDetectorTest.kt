package com.simprints.face.infra.simface.detection

import android.graphics.Bitmap
import com.google.common.truth.Truth.*
import com.simprints.biometrics.simface.SimFace
import com.simprints.biometrics.simface.data.FaceDetection
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SimFaceDetectorTest {
    @MockK
    lateinit var simFace: SimFace

    @MockK
    lateinit var image: Bitmap

    @MockK
    lateinit var faceDetection: FaceDetection

    lateinit var detector: SimFaceDetector

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        detector = SimFaceDetector(simFace)
    }

    @Test
    fun `returns null if no faces detected`() = runTest {
        coEvery { simFace.detectFaceBlocking(any()) } returns emptyList()
        assertThat(detector.analyze(image)).isNull()
    }

    @Test
    fun `returns null if low quality face`() = runTest {
        every { faceDetection.quality } returns 0.0f
        coEvery { simFace.detectFaceBlocking(any()) } returns listOf(faceDetection)
        assertThat(detector.analyze(image)).isNull()
    }

    @Test
    fun `returns face embedding for good quality face`() = runTest {
        every { faceDetection.quality } returns 0.8f
        every { faceDetection.alignedFaceImage(any()) } returns image
        every { simFace.getEmbedding(any()) } returns byteArrayOf(1, 2, 3, 4)
        coEvery { simFace.detectFaceBlocking(any()) } returns listOf(faceDetection)

        val face = detector.analyze(image)
        assertThat(face).isNotNull()
        assertThat(face?.quality).isEqualTo(0.8f)
        assertThat(face?.template).isEqualTo(byteArrayOf(1, 2, 3, 4))

        verify { simFace.getEmbedding(any()) }
    }
}
