package com.simprints.face.infra.simface.detection

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.common.truth.Truth.*
import com.simprints.biometrics.simface.SimFace
import com.simprints.biometrics.simface.data.FaceDetection
import com.simprints.face.infra.basebiosdk.detection.SpoofCheckResult
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

    @Test
    fun `extracts the first face when no selector is supplied`() = runTest {
        val first = face(quality = 0.8f)
        detects(first, face(quality = 0.9f))

        detector.analyze(image)

        verify(exactly = 1) { first.alignedFaceImage(image) }
    }

    @Test
    fun `extracts the face the selector chose`() = runTest {
        val chosen = face(quality = 0.9f)
        detects(face(quality = 0.8f), chosen, face(quality = 0.7f))

        detector.analyze(image, selectFace = { 1 })

        verify(exactly = 1) { chosen.alignedFaceImage(image) }
    }

    @Test
    fun `aligns and embeds the selected face alone`() = runTest {
        val chosen = face(quality = 0.9f)
        val ignored = listOf(face(quality = 0.8f), face(quality = 0.7f))
        detects(ignored[0], chosen, ignored[1])

        detector.analyze(image, selectFace = { 1 })

        ignored.forEach { verify(exactly = 0) { it.alignedFaceImage(any()) } }
        verify(exactly = 1) { simFace.getEmbedding(any()) }
    }

    @Test
    fun `never consults the selector when no faces were detected`() = runTest {
        coEvery { simFace.detectFaceBlocking(any()) } returns emptyList()
        var consulted = false

        detector.analyze(image, selectFace = {
            consulted = true
            0
        })

        assertThat(consulted).isFalse()
    }

    @Test
    fun `extracts nothing when the selector chooses none`() = runTest {
        detects(face(quality = 0.8f), face(quality = 0.9f))

        assertThat(detector.analyze(image, selectFace = { null })).isNull()
        verify(exactly = 0) { simFace.getEmbedding(any()) }
    }

    @Test
    fun `extracts nothing when the selector returns an out of range index`() = runTest {
        detects(face(quality = 0.8f), face(quality = 0.9f))

        assertThat(detector.analyze(image, selectFace = { 2 })).isNull()
        verify(exactly = 0) { simFace.getEmbedding(any()) }
    }

    @Test
    fun `applies the quality gate to the selected face, not the first one`() = runTest {
        detects(face(quality = 0.9f), face(quality = 0.0f))

        assertThat(detector.analyze(image, selectFace = { 1 })).isNull()
    }

    @Test
    fun `spoof check is not available on simface`() = runTest {
        val result = detector.spoofCheck(image, configuredMaxSize = 1000)

        assertThat(result.skipReason).isEqualTo(SpoofCheckResult.SkipReason.NOT_AVAILABLE)
        assertThat(result.score).isEqualTo(0f)
    }

    @Test
    fun `reports no age or gender, which simface does not estimate`() = runTest {
        detects(face(quality = 0.8f))

        val face = detector.analyze(image, estimateAgeAndGender = true)

        assertThat(face?.age).isNull()
        assertThat(face?.gender).isNull()
    }

    private fun detects(vararg faces: FaceDetection) {
        coEvery { simFace.detectFaceBlocking(any()) } returns faces.toList()
    }

    private fun face(
        quality: Float,
        yaw: Float = 0f,
        roll: Float = 0f,
        boundingBox: Rect = box(),
    ) = mockk<FaceDetection>(relaxed = true) {
        every { this@mockk.quality } returns quality
        every { this@mockk.yaw } returns yaw
        every { this@mockk.roll } returns roll
        every { absoluteBoundingBox } returns boundingBox
        every { alignedFaceImage(any()) } returns image
    }

    /** A distinct bounding box - only its identity matters, so it needs no real coordinates. */
    private fun box() = mockk<Rect>()
}
