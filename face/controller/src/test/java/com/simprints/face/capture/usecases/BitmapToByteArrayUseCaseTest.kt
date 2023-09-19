package com.simprints.face.capture.usecases

import android.graphics.Bitmap
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream

class BitmapToByteArrayUseCaseTest {

    private val bitmapMock = mockk<Bitmap> {
        every { compress(any(), any(), any()) } returns true
    }

    @Before
    fun setUp() {
        mockkStatic(Bitmap::class)
    }

    @After
    fun tearDown() {
        unmockkStatic(Bitmap::class)
    }

    @Test
    fun `Should return a compressed JPG Bitmap bytes`() {
        // When
        BitmapToByteArrayUseCase().invoke(bitmapMock)

        // Then
        verify {
            bitmapMock.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, any<ByteArrayOutputStream>())
        }
    }

    companion object {
        private const val IMAGE_QUALITY = 100
    }
}
