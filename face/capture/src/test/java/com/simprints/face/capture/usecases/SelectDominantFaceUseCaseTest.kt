package com.simprints.face.capture.usecases

import android.graphics.Rect
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class SelectDominantFaceUseCaseTest {
    private val useCase = SelectDominantFaceUseCase()

    /** Centred square of [side] px, offset from the middle of the frame by [offsetX]/[offsetY]. */
    private fun face(
        side: Int,
        offsetX: Int = 0,
        offsetY: Int = 0,
    ): Rect {
        val cx = FRAME / 2 + offsetX
        val cy = FRAME / 2 + offsetY
        return Rect(cx - side / 2, cy - side / 2, cx + side / 2, cy + side / 2)
    }

    @Test
    fun `no faces yields no selection`() {
        assertThat(useCase(emptyList(), FRAME, FRAME)).isNull()
    }

    @Test
    fun `unmeasured frame yields no selection`() {
        assertThat(useCase(listOf(face(300)), 0, 0)).isNull()
    }

    @Test
    fun `a lone face is the subject wherever it sits`() {
        assertThat(useCase(listOf(face(300)), FRAME, FRAME)).isEqualTo(0)
        assertThat(useCase(listOf(face(200, offsetX = 400, offsetY = 400)), FRAME, FRAME)).isEqualTo(0)
    }

    @Test
    fun `the clearly larger face wins`() {
        val small = face(120, offsetX = 300)
        val large = face(400)

        assertThat(useCase(listOf(small, large), FRAME, FRAME)).isEqualTo(1)
    }

    @Test
    fun `a centred face beats an equally sized one off to the side`() {
        val offToTheSide = face(250, offsetX = 420)
        val centred = face(250)

        assertThat(useCase(listOf(offToTheSide, centred), FRAME, FRAME)).isEqualTo(1)
    }

    @Test
    fun `a bystander does not outrank the subject being aimed at`() {
        val subject = face(420)
        val bystander = face(110, offsetX = 380, offsetY = -300)

        assertThat(useCase(listOf(subject, bystander), FRAME, FRAME)).isEqualTo(0)
    }

    @Test
    fun `two comparable faces still produce a winner rather than stalling`() {
        val slightlyBigger = face(270, offsetX = -200)
        val other = face(260, offsetX = 200)

        assertThat(useCase(listOf(slightlyBigger, other), FRAME, FRAME)).isEqualTo(0)
    }

    private companion object {
        const val FRAME = 1000
    }
}
