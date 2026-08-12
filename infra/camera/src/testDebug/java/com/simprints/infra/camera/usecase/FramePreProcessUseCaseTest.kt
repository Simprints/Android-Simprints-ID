package com.simprints.infra.camera.usecase

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.camera.repository.InjectedImageCache
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class FramePreProcessUseCaseTest {
    private lateinit var cache: InjectedImageCache
    private lateinit var useCase: FramePreProcessUseCase

    companion object {
        private val PREVIEW_RECT = Rect(0, 0, 600, 1200)
        private val TARGET_RECT_PORTRAIT = Rect(100, 200, 500, 1000)
        private val TARGET_RECT_LANDSCAPE = Rect(100, 400, 700, 800)
        private const val ROTATION = 90
    }

    @Before
    fun setUp() {
        cache = InjectedImageCache()
        useCase = FramePreProcessUseCase(cache)
    }

    @Test
    fun `returns original frame unchanged when no image is injected`() {
        val bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888)

        val frame = useCase(bitmap, ROTATION, PREVIEW_RECT, TARGET_RECT_PORTRAIT)

        assertThat(frame.bitmap).isEqualTo(bitmap)
        assertThat(frame.rotation).isEqualTo(ROTATION)
        assertThat(frame.previewBounds).isEqualTo(PREVIEW_RECT)
        assertThat(frame.targetBounds).isEqualTo(TARGET_RECT_PORTRAIT)
        assertThat(frame.isInjected).isFalse()
    }

    @Test
    fun `returns original frame after injected image is cleared from cache`() {
        val bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888)
        cache.injectedImage = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
        cache.injectedImage = null

        val frame = useCase(bitmap, ROTATION, PREVIEW_RECT, TARGET_RECT_PORTRAIT)

        assertThat(frame.bitmap).isEqualTo(bitmap)
        assertThat(frame.isInjected).isFalse()
    }

    @Test
    fun `produces frame at preview dimensions when image is injected`() {
        cache.injectedImage = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

        val frame = useCase(
            Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888),
            ROTATION,
            PREVIEW_RECT,
            TARGET_RECT_PORTRAIT,
        )

        assertThat(frame.bitmap.width).isEqualTo(PREVIEW_RECT.width())
        assertThat(frame.bitmap.height).isEqualTo(PREVIEW_RECT.height())
    }

    @Test
    fun `marks injected frame as injected with zero rotation`() {
        cache.injectedImage = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

        val frame = useCase(
            Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888),
            ROTATION,
            PREVIEW_RECT,
            TARGET_RECT_PORTRAIT,
        )

        assertThat(frame.isInjected).isTrue()
        assertThat(frame.rotation).isEqualTo(0)
    }

    @Test
    fun `preserves preview and target bounds in injected frame`() {
        cache.injectedImage = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

        val frame = useCase(
            Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888),
            ROTATION,
            PREVIEW_RECT,
            TARGET_RECT_PORTRAIT,
        )

        assertThat(frame.previewBounds).isEqualTo(PREVIEW_RECT)
        assertThat(frame.targetBounds).isEqualTo(TARGET_RECT_PORTRAIT)
    }

    @Test
    fun `scales injected image by target height when target is wider than tall`() {
        cache.injectedImage = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        val previewRect = Rect(0, 0, 1000, 1000)

        val frame = useCase(
            Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888),
            0,
            previewRect,
            TARGET_RECT_LANDSCAPE,
        )

        // Result is always sized to preview. Scale path selected by target aspect ratio
        assertThat(frame.bitmap.width).isEqualTo(1000)
        assertThat(frame.bitmap.height).isEqualTo(1000)
        assertThat(frame.isInjected).isTrue()
    }

    @Test
    fun `scales injected image by target width when target is taller than wide`() {
        cache.injectedImage = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        val previewRect = Rect(0, 0, 1000, 2000)

        val frame = useCase(
            Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888),
            0,
            previewRect,
            TARGET_RECT_PORTRAIT,
        )

        assertThat(frame.bitmap.width).isEqualTo(1000)
        assertThat(frame.bitmap.height).isEqualTo(2000)
        assertThat(frame.isInjected).isTrue()
    }
}
