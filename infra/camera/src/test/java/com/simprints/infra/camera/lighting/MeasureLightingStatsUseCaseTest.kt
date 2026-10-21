package com.simprints.infra.camera.lighting

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class MeasureLightingStatsUseCaseTest {
    private val useCase = MeasureLightingStatsUseCase()

    @Test
    fun `measures luminance of a uniform bitmap`() {
        val stats = useCase(uniformBitmap(Color.rgb(100, 100, 100)))

        assertThat(stats).isEqualTo(LightingStats(p5 = 100, median = 100, p95 = 100))
        assertThat(stats?.contrast).isEqualTo(0)
    }

    @Test
    fun `measures only the given region`() {
        // Dark left half, bright right half
        val bitmap = splitBitmap(left = Color.rgb(20, 20, 20), right = Color.rgb(200, 200, 200))

        assertThat(useCase(bitmap, RectF(0f, 0f, 0.5f, 1f))?.median).isEqualTo(20)
        assertThat(useCase(bitmap, RectF(0.5f, 0f, 1f, 1f))?.median).isEqualTo(200)
    }

    @Test
    fun `measures a large bitmap by sampling`() {
        val stats = useCase(uniformBitmap(Color.rgb(50, 50, 50), size = 900))

        assertThat(stats?.median).isEqualTo(50)
    }

    @Test
    fun `clamps a region that extends beyond the bitmap`() {
        val stats = useCase(uniformBitmap(Color.rgb(100, 100, 100)), RectF(-0.5f, -0.5f, 1.5f, 1.5f))

        assertThat(stats?.median).isEqualTo(100)
    }

    @Test
    fun `ignores the border`() {
        val bitmap = uniformBitmap(Color.WHITE, size = 100)
        val inner = IntArray(80 * 80) { Color.BLACK }
        bitmap.setPixels(inner, 0, 80, 10, 10, 80, 80)

        assertThat(useCase(bitmap, borderWidthPercent = 10)).isEqualTo(LightingStats(p5 = 0, median = 0, p95 = 0))
    }

    @Test
    fun `returns null for an empty region`() {
        assertThat(useCase(uniformBitmap(Color.WHITE), RectF(0.5f, 0.5f, 0.5f, 0.8f))).isNull()
    }

    @Test
    fun `returns null for an empty or recycled bitmap`() {
        val empty = mockk<Bitmap>(relaxed = true) {
            every { width } returns 0
            every { height } returns 10
        }
        val recycled = mockk<Bitmap>(relaxed = true) {
            every { isRecycled } returns true
        }

        assertThat(useCase(empty)).isNull()
        assertThat(useCase(recycled)).isNull()
    }

    @Test
    fun `reuses buffers across different sizes`() {
        assertThat(useCase(uniformBitmap(Color.rgb(10, 10, 10), size = 300))?.median).isEqualTo(10)
        assertThat(useCase(uniformBitmap(Color.rgb(60, 60, 60), size = 50))?.median).isEqualTo(60)
        assertThat(useCase(uniformBitmap(Color.rgb(90, 90, 90), size = 400))?.median).isEqualTo(90)
    }

    private fun uniformBitmap(
        color: Int,
        size: Int = 200,
    ): Bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
        setPixels(IntArray(size * size) { color }, 0, size, 0, 0, size, size)
    }

    private fun splitBitmap(
        left: Int,
        right: Int,
        size: Int = 200,
    ): Bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
        setPixels(IntArray(size * size) { if (it % size < size / 2) left else right }, 0, size, 0, 0, size, size)
    }
}
