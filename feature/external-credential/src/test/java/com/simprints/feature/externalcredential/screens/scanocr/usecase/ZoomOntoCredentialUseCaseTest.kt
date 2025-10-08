package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.common.truth.Truth.*
import com.simprints.feature.externalcredential.model.BoundingBox
import io.mockk.*
import org.junit.Before
import org.junit.Test

class ZoomOntoCredentialUseCaseTest {
    private lateinit var useCase: ZoomOntoCredentialUseCase
    private lateinit var mockBitmap: Bitmap
    private val bitmapWidth = 1600
    private val bitmapHeight = 1000 // 16:10
    private val imagePath = "path"

    @Before
    fun setUp() {
        useCase = ZoomOntoCredentialUseCase()
        mockkStatic(BitmapFactory::class)
        mockBitmap = mockk {
            every { width } returns bitmapWidth
            every { height } returns bitmapHeight
        }
        every { BitmapFactory.decodeFile(any()) } returns mockBitmap
    }

    @Test
    fun `returns original bitmap when bounding box has zero width`() {
        val boundingBox = BoundingBox(left = 100, top = 100, right = 100, bottom = 200)
        val result = useCase(imagePath, boundingBox)
        assertThat(result).isEqualTo(mockBitmap)
    }

    @Test
    fun `returns original bitmap when bounding box has zero height`() {
        val boundingBox = BoundingBox(left = 100, top = 100, right = 200, bottom = 100)
        val result = useCase(imagePath, boundingBox)
        assertThat(result).isEqualTo(mockBitmap)
    }

    @Test
    fun `adjusts crop area to fit 16 to 10 aspect ratio when box is wider`() {
        val boundingBox = BoundingBox(left = 100, top = 400, right = 500, bottom = 500)
        runAspectRatioTest(boundingBox)
    }

    @Test
    fun `adjusts crop area to fit 16 to 10 aspect ratio when box is taller`() {
        val boundingBox = BoundingBox(left = 400, top = 100, right = 500, bottom = 500)
        runAspectRatioTest(boundingBox)
    }

    @Test
    fun `clamps crop area to bitmap boundaries when expansion exceeds left edge`() {
        val boundingBox = BoundingBox(left = 10, top = 400, right = 110, bottom = 500)
        runEdgeClampingTest(boundingBox)
    }

    @Test
    fun `clamps crop area to bitmap boundaries when expansion exceeds right edge`() {
        val boundingBox = BoundingBox(left = 890, top = 400, right = 990, bottom = 500)
        runEdgeClampingTest(boundingBox)
    }

    @Test
    fun `clamps crop area to bitmap boundaries when expansion exceeds top edge`() {
        val boundingBox = BoundingBox(left = 400, top = 10, right = 500, bottom = 110)
        runEdgeClampingTest(boundingBox)
    }

    @Test
    fun `clamps crop area to bitmap boundaries when expansion exceeds bottom edge`() {
        val boundingBox = BoundingBox(left = 400, top = 890, right = 500, bottom = 990)
        runEdgeClampingTest(boundingBox)
    }

    @Test
    fun `handles bounding box in center of image correctly`() {
        val boundingBox = BoundingBox(left = 400, top = 400, right = 600, bottom = 500)
        val croppedBitmap = mockk<Bitmap>()
        val capturedLeft = slot<Int>()
        val capturedTop = slot<Int>()
        val capturedWidth = slot<Int>()
        val capturedHeight = slot<Int>()
        mockkStatic(Bitmap::class)
        every {
            Bitmap.createBitmap(
                any<Bitmap>(),
                capture(capturedLeft),
                capture(capturedTop),
                capture(capturedWidth),
                capture(capturedHeight),
            )
        } returns croppedBitmap
        useCase(imagePath, boundingBox)

        // Double checking that aspect ratio remains 16:10
        val aspectRatio = capturedWidth.captured.toFloat() / capturedHeight.captured.toFloat()
        assertThat(aspectRatio).isWithin(0.01f).of(1.6f)

        assertThat(capturedLeft.captured).isAtLeast(0)
        assertThat(capturedTop.captured).isAtLeast(0)
        assertThat(capturedLeft.captured + capturedWidth.captured).isAtMost(bitmapWidth)
        assertThat(capturedTop.captured + capturedHeight.captured).isAtMost(bitmapHeight)
    }

    private fun runAspectRatioTest(boundingBox: BoundingBox) {
        val croppedBitmap = mockk<Bitmap>()
        val capturedWidth = slot<Int>()
        val capturedHeight = slot<Int>()
        mockkStatic(Bitmap::class)
        every {
            Bitmap.createBitmap(any<Bitmap>(), any(), any(), capture(capturedWidth), capture(capturedHeight))
        } returns croppedBitmap

        useCase(imagePath, boundingBox)
        val aspectRatio = capturedWidth.captured.toFloat() / capturedHeight.captured.toFloat()
        assertThat(aspectRatio).isWithin(0.01f).of(1.6f)
    }

    private fun runEdgeClampingTest(boundingBox: BoundingBox) {
        val croppedBitmap = mockk<Bitmap>()
        val capturedLeft = slot<Int>()
        val capturedTop = slot<Int>()
        val capturedWidth = slot<Int>()
        val capturedHeight = slot<Int>()
        mockkStatic(Bitmap::class)
        every {
            Bitmap.createBitmap(
                any<Bitmap>(),
                capture(capturedLeft),
                capture(capturedTop),
                capture(capturedWidth),
                capture(capturedHeight),
            )
        } returns croppedBitmap

        useCase(imagePath, boundingBox)

        // Crop area should stay within the bitmap boundaries
        assertThat(capturedLeft.captured).isAtLeast(0)
        assertThat(capturedTop.captured).isAtLeast(0)
        assertThat(capturedLeft.captured + capturedWidth.captured).isAtMost(bitmapWidth)
        assertThat(capturedTop.captured + capturedHeight.captured).isAtMost(bitmapHeight)
    }

    private fun mockBitmapCreation(croppedBitmap: Bitmap) {
        mockkStatic(Bitmap::class)
        every { Bitmap.createBitmap(any<Bitmap>(), any(), any(), any(), any()) } returns croppedBitmap
    }
}
