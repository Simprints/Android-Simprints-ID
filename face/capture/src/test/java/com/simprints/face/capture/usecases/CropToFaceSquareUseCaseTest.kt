package com.simprints.face.capture.usecases

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
}
