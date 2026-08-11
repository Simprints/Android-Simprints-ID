package com.simprints.infra.camera.postprocess

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.test.ext.junit.runners.*
import com.google.common.truth.Truth.*
import com.simprints.infra.camera.Frame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class FrameCropToTargetUseCaseTest {
    lateinit var useCase: FrameCropToTargetUseCase

    @Before
    fun setUp() {
        useCase = FrameCropToTargetUseCase()
    }

    @Test
    fun `Skip cropping when target is empty`() {
        val cropped = useCase(
            Frame(
                bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888),
                rotation = 0,
                previewBounds = Rect(0, 0, 1000, 2000),
                targetBounds = Rect(200, 200, 200, 200), // i.e. empty
            ),
        )

        // Cropped should be same as original
        assertThat(cropped.width).isEqualTo(1000)
        assertThat(cropped.height).isEqualTo(1000)
    }

    @Test
    fun `Skip cropping when cutout rect empty after scaling`() {
        val cropped = useCase(
            Frame(
                bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888),
                rotation = 0,
                previewBounds = Rect(0, 0, 1000, 1000),
                targetBounds = Rect(1, 1, 2, 2), // Will scale to 0.1 pixel and round down
            ),
        )

        // Cropped should be same as original
        assertThat(cropped.width).isEqualTo(100)
        assertThat(cropped.height).isEqualTo(100)
    }

    @Test
    fun `Correctly crops when camera resolution is smaller than preview in portrait`() {
        val cropped = useCase(
            Frame(
                bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888),
                rotation = 0,
                previewBounds = Rect(0, 0, 1000, 2000),
                targetBounds = Rect(200, 200, 800, 800), // 600x600
            ),
        )

        // Cropped should be still square and half the side length of original
        assertThat(cropped.width).isEqualTo(300)
        assertThat(cropped.height).isEqualTo(300)
    }

    @Test
    fun `Correctly crops when camera resolution is smaller than preview in landscape`() {
        val cropped = useCase(
            Frame(
                bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888),
                rotation = 0,
                previewBounds = Rect(0, 0, 2000, 1000), // landscape
                targetBounds = Rect(700, 200, 1300, 800), // 600x600
            ),
        )

        // Cropped should be still square and half the side length of original
        assertThat(cropped.width).isEqualTo(300)
        assertThat(cropped.height).isEqualTo(300)
    }

    @Test
    fun `Correctly crops when camera resolution is larger than preview in portrait`() {
        val cropped = useCase(
            Frame(
                bitmap = Bitmap.createBitmap(2000, 2000, Bitmap.Config.ARGB_8888),
                rotation = 0,
                previewBounds = Rect(0, 0, 1000, 2000), // landscape
                targetBounds = Rect(200, 200, 800, 800), // 600x600
            ),
        )

        // Cropped should be still square
        assertThat(cropped.width).isEqualTo(600)
        assertThat(cropped.height).isEqualTo(600)
    }

    @Test
    fun `Correctly crops when camera resolution is larger than preview in landscape`() {
        val cropped = useCase(
            Frame(
                bitmap = Bitmap.createBitmap(2000, 2000, Bitmap.Config.ARGB_8888),
                rotation = 0,
                previewBounds = Rect(0, 0, 2000, 1000), // landscape
                targetBounds = Rect(700, 200, 1300, 800), // 600x600
            ),
        )

        // Cropped should be still square and half the side length of original
        assertThat(cropped.width).isEqualTo(600)
        assertThat(cropped.height).isEqualTo(600)
    }

    @Test
    fun `Correctly crops bitmap with valid cutout rectangle`() {
        val cropped = useCase(
            Frame(
                bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888),
                rotation = 0,
                previewBounds = Rect(0, 0, 1080, 1920),
                targetBounds = Rect(200, 300, 800, 1500),
            ),
        )

        // Cropped should be same size as target
        assertThat(cropped.width).isEqualTo(600)
        assertThat(cropped.height).isEqualTo(1200)
    }

    @Test
    fun `Correctly clamps cutout rect that extends beyond bitmap bounds`() {
        val cropped = useCase(
            Frame(
                bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888),
                rotation = 0,
                previewBounds = Rect(0, 0, 1080, 1920),
                targetBounds = Rect(-100, -200, 1280, 2020),
            ),
        )

        // Cropped should be same size as preview
        assertThat(cropped.width).isEqualTo(1080)
        assertThat(cropped.height).isEqualTo(1920)
    }
}
