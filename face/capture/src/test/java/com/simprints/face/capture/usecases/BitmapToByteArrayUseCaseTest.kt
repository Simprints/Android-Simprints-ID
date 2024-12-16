package com.simprints.face.capture.usecases

import android.content.Context
import android.graphics.Bitmap
import android.util.TypedValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BitmapToByteArrayUseCaseTest {
    @RelaxedMockK
    private lateinit var context: Context

    private lateinit var bitmapToByteArray: BitmapToByteArrayUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(Bitmap::class)
        mockkStatic(TypedValue::class)
        every {
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, any(), any())
        } returns 240f // mock 240 dp

        bitmapToByteArray = BitmapToByteArrayUseCase(context)
    }

    @Test
    fun `invoke should resized and compressed the bitmap`() {
        // Given
        val mockBitmap: Bitmap = mockk(relaxed = true) {
            every { width } returns 500
            every { height } returns 500
        }
        val resizedBitmap: Bitmap = mockk(relaxed = true) {
            every {
                compress(
                    Bitmap.CompressFormat.JPEG,
                    BitmapToByteArrayUseCase.IMAGE_QUALITY,
                    any(),
                )
            } returns true
        }
        every { Bitmap.createScaledBitmap(any(), any(), any(), any()) } returns resizedBitmap

        // When
        bitmapToByteArray(mockBitmap)

        // Then
        verify { mockBitmap.recycle() }
        verify { resizedBitmap.recycle() }
        verify {
            resizedBitmap.compress(
                Bitmap.CompressFormat.JPEG,
                BitmapToByteArrayUseCase.IMAGE_QUALITY,
                any(),
            )
        }
    }

    @Test
    fun `invoke should use original bitmap if size is under the limit`() {
        // Given
        val mockBitmap: Bitmap = mockk(relaxed = true)
        every { mockBitmap.width } returns 100
        every { mockBitmap.height } returns 100

        // When
        bitmapToByteArray(mockBitmap)
        // Then
        verify(exactly = 0) { Bitmap.createScaledBitmap(any(), any(), any(), any()) }
    }

    @Test
    fun `resizeBitmap should scale down bitmap if size exceeds the maximum`() {
        // Given
        val mockBitmap: Bitmap = mockk(relaxed = true)
        val scaledBitmap: Bitmap = mockk(relaxed = true)

        // Mock bitmap size
        every { mockBitmap.width } returns 500
        every { mockBitmap.height } returns 500

        // Mock scaling behavior
        mockkStatic(Bitmap::class)
        every { Bitmap.createScaledBitmap(mockBitmap, any(), any(), any()) } returns scaledBitmap

        // When
        bitmapToByteArray(mockBitmap)

        // Then
        verify {
            Bitmap.createScaledBitmap(mockBitmap, 240, 240, true)
        }
    }
}
