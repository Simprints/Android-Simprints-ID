package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class CropDocumentFromPreviewUseCaseTest {
    @MockK
    lateinit var sourceBitmap: Bitmap

    @MockK
    lateinit var croppedBitmap: Bitmap

    private lateinit var useCase: CropDocumentFromPreviewUseCase

    private val bitmapWidth = 1080
    private val bitmapHeight = 1920

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { sourceBitmap.width } returns bitmapWidth
        every { sourceBitmap.height } returns bitmapHeight

        useCase = CropDocumentFromPreviewUseCase()
    }

    @Test
    fun `crops bitmap with valid cutout rect`() {
        val cutoutRect = Rect(200, 300, 800, 1500)

        mockkStatic(Bitmap::class)
        every {
            Bitmap.createBitmap(
                sourceBitmap,
                cutoutRect.left,
                cutoutRect.top,
                cutoutRect.width(),
                cutoutRect.height(),
            )
        } returns croppedBitmap

        val result = useCase(sourceBitmap, cutoutRect)

        assertThat(result).isEqualTo(croppedBitmap)
        verify {
            Bitmap.createBitmap(
                sourceBitmap,
                cutoutRect.left,
                cutoutRect.top,
                cutoutRect.width(),
                cutoutRect.height(),
            )
        }
        unmockkStatic(Bitmap::class)
    }

    @Test
    fun `clamps cutout rect that extends beyond bitmap bounds`() {
        val cutoutRect = Rect(-100, -50, bitmapWidth + 200, bitmapHeight + 100)
        val expectedLeft = 0
        val expectedTop = 0
        val expectedRight = bitmapWidth
        val expectedBottom = bitmapHeight

        mockkStatic(Bitmap::class)
        every {
            Bitmap.createBitmap(
                sourceBitmap,
                expectedLeft,
                expectedTop,
                expectedRight - expectedLeft,
                expectedBottom - expectedTop,
            )
        } returns croppedBitmap

        val result = useCase(sourceBitmap, cutoutRect)

        assertThat(result).isEqualTo(croppedBitmap)
        verify {
            Bitmap.createBitmap(
                sourceBitmap,
                expectedLeft,
                expectedTop,
                expectedRight - expectedLeft,
                expectedBottom - expectedTop,
            )
        }
        unmockkStatic(Bitmap::class)
    }

    @Test
    fun `handles cutout rect with negative coordinates`() {
        val left = -100
        val top = -200
        val right = 500
        val bottom = 1000
        val expectedLeft = 0
        val expectedTop = 0
        val expectedWidth = right - expectedLeft
        val expectedHeight = bottom - expectedTop
        val cutoutRect = Rect(left, top, right, bottom)

        mockkStatic(Bitmap::class)
        every {
            Bitmap.createBitmap(
                sourceBitmap,
                expectedLeft,
                expectedTop,
                expectedWidth,
                expectedHeight,
            )
        } returns croppedBitmap

        val result = useCase(sourceBitmap, cutoutRect)

        assertThat(result).isEqualTo(croppedBitmap)
        verify {
            Bitmap.createBitmap(
                sourceBitmap,
                expectedLeft,
                expectedTop,
                expectedWidth,
                expectedHeight,
            )
        }
        unmockkStatic(Bitmap::class)
    }

    @Test
    fun `handles cutout rect exceeding right and bottom bounds`() {
        val left = 500
        val top = 1000
        val cutoutRect = Rect(left, top, bitmapWidth + 100, bitmapHeight + 200)
        val expectedRight = bitmapWidth
        val expectedBottom = bitmapHeight
        val expectedWidth = expectedRight - left
        val expectedHeight = expectedBottom - top

        mockkStatic(Bitmap::class)
        every {
            Bitmap.createBitmap(
                sourceBitmap,
                left,
                top,
                expectedWidth,
                expectedHeight,
            )
        } returns croppedBitmap

        val result = useCase(sourceBitmap, cutoutRect)

        assertThat(result).isEqualTo(croppedBitmap)
        verify {
            Bitmap.createBitmap(
                sourceBitmap,
                left,
                top,
                expectedWidth,
                expectedHeight,
            )
        }
        unmockkStatic(Bitmap::class)
    }
}
