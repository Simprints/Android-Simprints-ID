package com.simprints.infra.camera.usecase

import android.graphics.Rect
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class GetTargetRegionInPhotoUseCaseTest {
    private val useCase = GetTargetRegionInPhotoUseCase()

    @Test
    fun `maps target onto the centre of a wider upright photo`() {
        val region = useCase(
            photoWidth = 2000,
            photoHeight = 2000,
            rotationDegrees = 0,
            previewRect = Rect(0, 0, 1000, 2000),
            targetRect = Rect(200, 200, 800, 800),
        )

        // Preview maps 1:1 onto the photo, centred horizontally with a 500 px margin
        assertThat(region).isEqualTo(Rect(700, 200, 1300, 800))
    }

    @Test
    fun `keeps the photo's native resolution`() {
        val region = useCase(
            photoWidth = 2160,
            photoHeight = 4062,
            rotationDegrees = 0,
            previewRect = Rect(0, 0, 720, 1354),
            targetRect = Rect(28, 470, 692, 885),
        )

        assertThat(region.width()).isEqualTo(1992)
        assertThat(region.height()).isEqualTo(1245)
    }

    @Test
    fun `turns the window back into raw coordinates for a photo rotated by 90 degrees`() {
        val region = useCase(
            photoWidth = 4000,
            photoHeight = 3000,
            rotationDegrees = 90,
            previewRect = Rect(0, 0, 1500, 2000),
            targetRect = Rect(0, 0, 1500, 1000),
        )

        // Top half of the upright photo is the left half of the raw one
        assertThat(region).isEqualTo(Rect(0, 0, 2000, 3000))
    }

    @Test
    fun `turns the window back into raw coordinates for a photo rotated by 270 degrees`() {
        val region = useCase(
            photoWidth = 4000,
            photoHeight = 3000,
            rotationDegrees = 270,
            previewRect = Rect(0, 0, 1500, 2000),
            targetRect = Rect(0, 0, 1500, 1000),
        )

        // Top half of the upright photo is the right half of the raw one
        assertThat(region).isEqualTo(Rect(2000, 0, 4000, 3000))
    }

    @Test
    fun `clamps a target that spills outside the preview`() {
        val region = useCase(
            photoWidth = 1000,
            photoHeight = 1000,
            rotationDegrees = 0,
            previewRect = Rect(0, 0, 1000, 1000),
            targetRect = Rect(-100, -100, 1200, 1200),
        )

        assertThat(region).isEqualTo(Rect(0, 0, 1000, 1000))
    }
}
