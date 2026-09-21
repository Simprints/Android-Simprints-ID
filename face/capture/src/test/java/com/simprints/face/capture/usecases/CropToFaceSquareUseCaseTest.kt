package com.simprints.face.capture.usecases

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class CropToFaceSquareUseCaseTest {
    private val useCase = CropToFaceSquareUseCase()

    @Test
    fun `square takes the longer detection side and keeps the detection centre`() {
        val square = useCase.squareFor(RectF(400f, 300f, 600f, 700f), 1000, 1000)

        assertThat(square).isEqualTo(Rect(300, 300, 700, 700))
    }

    @Test
    fun `already square detections are returned unchanged`() {
        val square = useCase.squareFor(RectF(100f, 200f, 400f, 500f), 1000, 1000)

        assertThat(square).isEqualTo(Rect(100, 200, 400, 500))
    }

    @Test
    fun `square overhanging an edge is shifted inwards at full size`() {
        val square = useCase.squareFor(RectF(-50f, 100f, 150f, 300f), 1000, 1000)

        assertThat(square).isEqualTo(Rect(0, 100, 200, 300))
    }

    @Test
    fun `square overhanging two edges is shifted inwards on both axes`() {
        val square = useCase.squareFor(RectF(900f, 900f, 1100f, 1100f), 1000, 1000)

        assertThat(square).isEqualTo(Rect(800, 800, 1000, 1000))
    }

    @Test
    fun `square larger than the frame is capped at the shorter frame dimension`() {
        val square = useCase.squareFor(RectF(-100f, -100f, 900f, 900f), 800, 600)

        assertThat(square.width()).isEqualTo(600)
        assertThat(square.height()).isEqualTo(600)
        assertThat(square).isEqualTo(Rect(100, 0, 700, 600))
    }

    @Test
    fun `square is always fully inside a non-square frame`() {
        val square = useCase.squareFor(RectF(700f, 10f, 900f, 210f), 800, 1600)

        assertThat(square.left).isAtLeast(0)
        assertThat(square.top).isAtLeast(0)
        assertThat(square.right).isAtMost(800)
        assertThat(square.bottom).isAtMost(1600)
        assertThat(square.width()).isEqualTo(square.height())
    }

    @Test
    fun `empty detection yields an empty square`() {
        assertThat(useCase.squareFor(RectF(100f, 100f, 100f, 100f), 1000, 1000).isEmpty).isTrue()
    }

    @Test
    fun `unmeasured frame yields an empty square`() {
        assertThat(useCase.squareFor(RectF(0f, 0f, 200f, 200f), 0, 0).isEmpty).isTrue()
    }

    @Test
    fun `an oversized crop is scaled down to the cap before it is kept`() {
        val crop = useCase(frame(1000, 1000), Rect(100, 100, 700, 700))

        // 600px of face is more than any template needs, and it is stored and uploaded as-is
        assertThat(crop.width).isEqualTo(CropToFaceSquareUseCase.MAX_CROP_SIZE_PX)
        assertThat(crop.height).isEqualTo(CropToFaceSquareUseCase.MAX_CROP_SIZE_PX)
    }

    @Test
    fun `a crop within the cap is kept at its own size`() {
        val crop = useCase(frame(1000, 1000), Rect(0, 0, 250, 250))

        assertThat(crop.width).isEqualTo(250)
        assertThat(crop.height).isEqualTo(250)
    }

    @Test
    fun `a crop exactly at the cap is left alone`() {
        val size = CropToFaceSquareUseCase.MAX_CROP_SIZE_PX
        val crop = useCase(frame(1000, 1000), Rect(0, 0, size, size))

        assertThat(crop.width).isEqualTo(size)
        assertThat(crop.height).isEqualTo(size)
    }

    @Test
    fun `a square that fits but hangs off the edge is refused rather than cropped`() {
        val source = frame(1000, 1000)

        // 300px wide and so small enough for the frame, but its right edge is past it
        val crop = useCase(source, Rect(800, 100, 1100, 400))

        assertThat(crop).isSameInstanceAs(source)
    }

    @Test
    fun `a square flush against the far edge is still cropped`() {
        val crop = useCase(frame(1000, 1000), Rect(700, 700, 1000, 1000))

        assertThat(crop.width).isEqualTo(300)
        assertThat(crop.height).isEqualTo(300)
    }

    @Test
    fun `scaling down leaves the caller's frame and the returned crop usable`() {
        val source = frame(1000, 1000)

        val crop = useCase(source, Rect(0, 0, 800, 800))

        // Only the intermediate full-size crop is the use case's to release
        assertThat(source.isRecycled).isFalse()
        assertThat(crop.isRecycled).isFalse()
    }

    private fun frame(
        width: Int,
        height: Int,
    ) = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
}
