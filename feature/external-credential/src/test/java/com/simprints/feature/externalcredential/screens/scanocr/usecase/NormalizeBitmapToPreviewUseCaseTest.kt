package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import android.graphics.Matrix
import com.google.common.truth.Truth.assertThat
import android.graphics.Rect
import androidx.core.graphics.scale
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrCropConfig
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

internal class NormalizeBitmapToPreviewUseCaseTest {

    @MockK
    private lateinit var inputBitmap: Bitmap

    @MockK
    private lateinit var rotatedBitmap: Bitmap

    @MockK
    private lateinit var croppedBitmap: Bitmap

    @MockK
    private lateinit var scaledBitmap: Bitmap

    private lateinit var useCase: NormalizeBitmapToPreviewUseCase

    private val inputWidth = 1920
    private val inputHeight = 1080
    private val previewWidth = 800
    private val previewHeight = 600
    private val rotationDegrees = 90

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        mockkStatic(Bitmap::class)
        mockkStatic("androidx.core.graphics.BitmapKt")
        mockkConstructor(Matrix::class)

        every { inputBitmap.width } returns inputWidth
        every { inputBitmap.height } returns inputHeight
        every { rotatedBitmap.width } returns inputHeight
        every { rotatedBitmap.height } returns inputWidth
        every { anyConstructed<Matrix>().postRotate(any()) } returns true
        every { croppedBitmap.scale(any(), any()) } returns scaledBitmap

        useCase = NormalizeBitmapToPreviewUseCase()
    }

    @After
    fun tearDown() {
        unmockkStatic(Bitmap::class)
        unmockkStatic("androidx.core.graphics.BitmapKt")
        unmockkConstructor(Matrix::class)
    }
    @Test
    fun `returns original bitmap when no rotation is needed`() = runTest {
        val cropConfig = createCropConfig(rotationDegrees = 0)

        every {
            Bitmap.createBitmap(inputBitmap, any(), any(), any(), any())
        } returns croppedBitmap

        val result = useCase(inputBitmap, cropConfig)

        assertThat(result).isEqualTo(scaledBitmap)
        verify(exactly = 0) { Bitmap.createBitmap(inputBitmap, 0, 0, inputWidth, inputHeight, any(), true) }
        verify { Bitmap.createBitmap(inputBitmap, any(), any(), any(), any()) }
        verify { croppedBitmap.scale(previewWidth, previewHeight) }
    }

    @Test
    fun `returns scaled bitmap after rotation cropping and scaling`() = runTest {
        val cropConfig = createCropConfig(rotationDegrees)

        every {
            Bitmap.createBitmap(inputBitmap, 0, 0, inputWidth, inputHeight, any(), true)
        } returns rotatedBitmap

        every {
            Bitmap.createBitmap(rotatedBitmap, any(), any(), any(), any())
        } returns croppedBitmap

        val result = useCase(inputBitmap, cropConfig)

        assertThat(result).isEqualTo(scaledBitmap)
        verify { Bitmap.createBitmap(inputBitmap, 0, 0, inputWidth, inputHeight, any(), true) }
        verify { croppedBitmap.scale(previewWidth, previewHeight) }
    }

    private fun createCropConfig(rotationDegrees: Int) = OcrCropConfig(
        rotationDegrees = rotationDegrees,
        cutoutRect = Rect(0, 0, 100, 100),
        previewViewWidth = previewWidth,
        previewViewHeight = previewHeight
    )
}
