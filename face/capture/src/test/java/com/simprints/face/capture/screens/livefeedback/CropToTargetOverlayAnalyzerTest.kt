package com.simprints.face.capture.screens.livefeedback

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.camera.core.ImageProxy
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.face.capture.screens.livefeedback.views.CameraTargetOverlay
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class CropToTargetOverlayAnalyzerTest {

    @MockK
    lateinit var targetOverlay: CameraTargetOverlay

    @MockK
    lateinit var imageProxy: ImageProxy

    lateinit var analyzer: CropToTargetOverlayAnalyzer
    var capturedBitmap: Bitmap? = null

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        justRun { imageProxy.close() }

        capturedBitmap = null
        analyzer = CropToTargetOverlayAnalyzer(targetOverlay) { capturedBitmap = it }
    }

    @Test
    fun `Skip cropping when target is empty`() {
        // Target is a square 600x600px with 200px from top bounds
        setupScreenSize(1000, 2000)
        every { targetOverlay.circleRect } returns RectF(200f, 200f, 200f, 200f)
        setupImageSize(1000, 1000)

        analyzer.analyze(imageProxy)

        // Cropped should be still square and half the side length of original
        assertThat(capturedBitmap?.width).isNull()
        assertThat(capturedBitmap?.height).isNull()
    }

    @Test
    fun `Correctly crops when camera resolution is smaller than preview in portrait`() {
        // Target is a square 600x600px with 200px from top bounds
        setupScreenSize(1000, 2000)
        every { targetOverlay.circleRect } returns RectF(200f, 200f, 800f, 800f)
        setupImageSize(1000, 1000)

        analyzer.analyze(imageProxy)

        // Cropped should be still square and half the side length of original
        assertThat(capturedBitmap?.width).isEqualTo(300)
        assertThat(capturedBitmap?.height).isEqualTo(300)
    }

    @Test
    fun `Correctly crops when camera resolution is smaller than preview in landscape`() {
        // Target is a square 600x600px with 200px from top bounds
        setupScreenSize(2000, 1000)
        every { targetOverlay.circleRect } returns RectF(700f, 200f, 1300f, 800f)
        setupImageSize(1000, 1000)

        analyzer.analyze(imageProxy)

        // Cropped should be still square and half the side length of original
        assertThat(capturedBitmap?.width).isEqualTo(300)
        assertThat(capturedBitmap?.height).isEqualTo(300)
    }

    @Test
    fun `Correctly crops when camera resolution is larger than preview in portrait`() {
        // Target is a square 600x600px with 200px from top bounds
        setupScreenSize(1000, 2000)
        every { targetOverlay.circleRect } returns RectF(200f, 200f, 800f, 800f)
        setupImageSize(2000, 2000)

        analyzer.analyze(imageProxy)

        // Cropped should be still square and half the side length of original
        assertThat(capturedBitmap?.width).isEqualTo(600)
        assertThat(capturedBitmap?.height).isEqualTo(600)
    }


    @Test
    fun `Correctly crops when camera resolution is larger than preview in landscape`() {
        // Target is a square 600x600px with 200px from top bounds
        setupScreenSize(2000, 1000)
        every { targetOverlay.circleRect } returns RectF(700f, 200f, 1300f, 800f)
        setupImageSize(2000, 2000)

        analyzer.analyze(imageProxy)

        // Cropped should be still square and half the side length of original
        assertThat(capturedBitmap?.width).isEqualTo(600)
        assertThat(capturedBitmap?.height).isEqualTo(600)
    }

    private fun setupScreenSize(width: Int, height: Int) {
        every { targetOverlay.width } returns width
        every { targetOverlay.height } returns height
    }

    private fun setupImageSize(width: Int, height: Int) {
        every { imageProxy.toBitmap() } returns Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        every { imageProxy.width } returns width
        every { imageProxy.height } returns height
    }

}
