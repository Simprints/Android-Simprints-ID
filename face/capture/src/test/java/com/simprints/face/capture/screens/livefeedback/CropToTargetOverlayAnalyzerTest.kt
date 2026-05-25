package com.simprints.face.capture.screens.livefeedback

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.camera.core.ImageProxy
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class CropToTargetOverlayAnalyzerTest {
    @MockK
    lateinit var imageProxy: ImageProxy

    lateinit var analyzer: CropToTargetOverlayAnalyzer
    var capturedBitmap: Bitmap? = null

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        justRun { imageProxy.close() }
        capturedBitmap = null
    }

    @Test
    fun `Skip cropping when target is empty`() {
        setupImageSize(1000, 1000)
        analyzer = CropToTargetOverlayAnalyzer(
            previewRect = RectF(200f, 200f, 200f, 200f),
            overlayWidth = 1000,
            overlayHeight = 2000,
        ) { capturedBitmap = it }

        analyzer.analyze(imageProxy)

        // Cropped should be still square and half the side length of original
        assertThat(capturedBitmap?.width).isNull()
        assertThat(capturedBitmap?.height).isNull()
        verify(exactly = 1) { imageProxy.close() }
    }

    @Test
    fun `Correctly crops when camera resolution is smaller than preview in portrait`() {
        setupImageSize(1000, 1000)
        analyzer = CropToTargetOverlayAnalyzer(
            previewRect = RectF(200f, 200f, 800f, 800f),
            overlayWidth = 1000,
            overlayHeight = 2000,
        ) { capturedBitmap = it }

        analyzer.analyze(imageProxy)

        // Cropped should be still square and half the side length of original
        assertThat(capturedBitmap?.width).isEqualTo(300)
        assertThat(capturedBitmap?.height).isEqualTo(300)
    }

    @Test
    fun `Closes ImageProxy before invoking cropped callback`() {
        setupImageSize(1000, 1000)
        var closed = false
        every { imageProxy.close() } answers { closed = true }
        var closedBeforeCallback = false

        val analyzer = CropToTargetOverlayAnalyzer(
            previewRect = RectF(200f, 200f, 800f, 800f),
            overlayWidth = 1000,
            overlayHeight = 2000,
        ) { closedBeforeCallback = closed }

        analyzer.analyze(imageProxy)

        assertThat(closedBeforeCallback).isTrue()
        verify(exactly = 1) { imageProxy.close() }
    }

    @Test
    fun `Correctly crops when camera resolution is smaller than preview in landscape`() {
        setupImageSize(1000, 1000)
        analyzer = CropToTargetOverlayAnalyzer(
            previewRect = RectF(700f, 200f, 1300f, 800f),
            overlayWidth = 2000,
            overlayHeight = 1000,
        ) { capturedBitmap = it }

        analyzer.analyze(imageProxy)

        // Cropped should be still square and half the side length of original
        assertThat(capturedBitmap?.width).isEqualTo(300)
        assertThat(capturedBitmap?.height).isEqualTo(300)
    }

    @Test
    fun `Correctly crops when camera resolution is larger than preview in portrait`() {
        setupImageSize(2000, 2000)
        analyzer = CropToTargetOverlayAnalyzer(
            previewRect = RectF(200f, 200f, 800f, 800f),
            overlayWidth = 1000,
            overlayHeight = 2000,
        ) { capturedBitmap = it }

        analyzer.analyze(imageProxy)

        // Cropped should be still square and half the side length of original
        assertThat(capturedBitmap?.width).isEqualTo(600)
        assertThat(capturedBitmap?.height).isEqualTo(600)
    }

    @Test
    fun `Correctly crops when camera resolution is larger than preview in landscape`() {
        setupImageSize(2000, 2000)
        analyzer = CropToTargetOverlayAnalyzer(
            previewRect = RectF(700f, 200f, 1300f, 800f),
            overlayWidth = 2000,
            overlayHeight = 1000,
        ) { capturedBitmap = it }

        analyzer.analyze(imageProxy)

        // Cropped should be still square and half the side length of original
        assertThat(capturedBitmap?.width).isEqualTo(600)
        assertThat(capturedBitmap?.height).isEqualTo(600)
    }

    private fun setupImageSize(
        width: Int,
        height: Int,
    ) {
        every { imageProxy.toBitmap() } returns Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        every { imageProxy.width } returns width
        every { imageProxy.height } returns height
    }
}
