package com.simprints.face.capture

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import com.google.common.truth.Truth
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream

class BitmapExtTest {
    companion object {
        const val IMAGE_WIDTH = 150
        const val IMAGE_HEIGHT = 150

        const val CROP_RECT_TOP = 0
        const val CROP_RECT_LEFT = 0
        const val CROP_RECT_RIGHT = 100
        const val CROP_RECT_BOTTOM = 100

        const val BYTEARRAY_SIZE = IMAGE_WIDTH * IMAGE_HEIGHT * 4
        const val IMAGE_QUALITY = 100
    }

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

    private val bitmapMock = mockk<Bitmap> {
        justRun { copyPixelsFromBuffer(any()) }
        justRun { recycle() }
        every { config } returns Bitmap.Config.ARGB_8888
        every {
            compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, any<ByteArrayOutputStream>())
        } returns true
    }

    @Before
    fun setup() {
        mockkStatic(Bitmap::class)
        every { Bitmap.createBitmap(any(), any(), any()) } returns bitmapMock
        every {
            Bitmap.createBitmap(
                any(), any(), any(), any(), any(), any(), true
            )
        } returns bitmapMock
    }

    @Test
    fun `toBitmap() should return a Bitmap with the same pixel format as the ImageProxy`() {
        // When
        val bitmap = imageProxy.toBitmap(
            Rect(CROP_RECT_LEFT, CROP_RECT_RIGHT, CROP_RECT_RIGHT, CROP_RECT_BOTTOM)
        )
        // Then
        Truth.assertThat(bitmap.config).isEqualTo(Bitmap.Config.ARGB_8888)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toBitmap() should throw if the pixel format is not RGBA_8888`() {
        // Given
        every { imageProxy.format } returns ImageFormat.YUV_420_888

        // When
        imageProxy.toBitmap(Rect(CROP_RECT_LEFT, CROP_RECT_TOP, CROP_RECT_RIGHT, CROP_RECT_BOTTOM))
        // Then throws  IllegalArgumentException
    }

    @Test
    fun `toByteArray() should return a compressed JPG Bitmap bytes`() {

        // When
        bitmapMock.toJpgCompressedByteArray()

        // Then
        verify {
            bitmapMock.compress(
                Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, any<ByteArrayOutputStream>()
            )
        }
    }

}
