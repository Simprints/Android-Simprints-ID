package com.simprints.face.capture.usecases

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageProxyToBitmapUseCaseTest {

    private var imageBytes: ByteArray = ByteArray(BYTEARRAY_SIZE)
    private val imageProxy = mockk<ImageProxy> {
        every { width } returns IMAGE_WIDTH
        every { height } returns IMAGE_HEIGHT
        every { format } returns android.graphics.PixelFormat.RGBA_8888
        every { imageInfo.rotationDegrees } returns 0
        every { planes } returns arrayOf(mockk {
            every { buffer } returns java.nio.ByteBuffer.wrap(imageBytes)
            every { pixelStride } returns 1
            every { rowStride } returns IMAGE_WIDTH
        })
    }

    private lateinit var useCase: ImageProxyToBitmapUseCase

    private val bitmapMock = mockk<Bitmap> {
        justRun { copyPixelsFromBuffer(any()) }
        justRun { recycle() }
    }

    @Before
    fun setup() {
        mockkStatic(Bitmap::class)
        every { Bitmap.createBitmap(any(), any(), any()) } returns bitmapMock
        every { Bitmap.createBitmap(any(), any(), any(), any(), any(), any(), true) } returns bitmapMock

        useCase = ImageProxyToBitmapUseCase()
    }

    @After
    fun tearDown() {
        unmockkStatic(Bitmap::class)
    }

    @Test
    fun `Should return a Bitmap with the same pixel format as the ImageProxy`() {
        // When
        val bitmap = useCase.invoke(
            imageProxy,
            Rect(CROP_RECT_LEFT, CROP_RECT_TOP, CROP_RECT_RIGHT, CROP_RECT_BOTTOM)
        )
        // Then
        assertThat(bitmap).isNotNull()
    }

    @Test
    fun `Should return a null if cropRect is empty`() {

        val rect = Rect(CROP_RECT_LEFT, CROP_RECT_TOP, CROP_RECT_LEFT, CROP_RECT_TOP)
        // When
        val bitmap = useCase.invoke(imageProxy, rect)
        // Then
        assertThat(bitmap).isNull()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw if the pixel format is not RGBA_8888`() {
        // Given
        every { imageProxy.format } returns ImageFormat.YUV_420_888

        // When
        useCase.invoke(
            imageProxy,
            Rect(CROP_RECT_LEFT, CROP_RECT_TOP, CROP_RECT_RIGHT, CROP_RECT_BOTTOM)
        )
        // Then throws  IllegalArgumentException
    }

    companion object {

        const val IMAGE_WIDTH = 150
        const val IMAGE_HEIGHT = 150

        const val CROP_RECT_LEFT = 0
        const val CROP_RECT_TOP = 0
        const val CROP_RECT_RIGHT = 100
        const val CROP_RECT_BOTTOM = 100

        const val BYTEARRAY_SIZE = IMAGE_WIDTH * IMAGE_HEIGHT * 4

    }
}
