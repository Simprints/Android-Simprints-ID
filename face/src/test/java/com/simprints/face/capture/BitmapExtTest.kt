package com.simprints.face.capture

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals

class BitmapExtTest {

    private var imageBytes: ByteArray = ByteArray(150 * 200 * 4)
    private val imageProxy = mockk<ImageProxy> {
        val imageWidth = 150
        val imageHeight = 200
        every { width } returns imageWidth
        every { height } returns imageHeight
        every { format } returns android.graphics.PixelFormat.RGBA_8888
        every { imageInfo.rotationDegrees } returns 0
        every { planes } returns arrayOf(mockk {
            every { buffer } returns java.nio.ByteBuffer.wrap(imageBytes)
            every { pixelStride } returns 1
            every { rowStride } returns imageWidth
        })
    }

    private val bitmapMock = mockk<Bitmap> {
        justRun { copyPixelsFromBuffer(any()) }
        justRun { recycle() }
        every { config } returns Bitmap.Config.ARGB_8888
        every {
            compress(Bitmap.CompressFormat.JPEG, 100, any<ByteArrayOutputStream>())
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
        val bitmap = imageProxy.toBitmap(Rect(0, 0, 100, 100))
        // Then
        assertEquals(Bitmap.Config.ARGB_8888, bitmap.config)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toBitmap() should throw if the pixel format is not RGBA_8888`() {
        // Given
        every { imageProxy.format } returns ImageFormat.YUV_420_888

        // When
        imageProxy.toBitmap(Rect(0, 0, 100, 100))

        // Then throws  IllegalArgumentException
    }

    @Test
    fun `toByteArray() should return a compressed JPG Bitmap bytes`() {

        // When
        bitmapMock.toByteArray()

        // Then
        verify {
            bitmapMock.compress(
                Bitmap.CompressFormat.JPEG,
                100,
                any<ByteArrayOutputStream>()
            )
        }
    }

}
