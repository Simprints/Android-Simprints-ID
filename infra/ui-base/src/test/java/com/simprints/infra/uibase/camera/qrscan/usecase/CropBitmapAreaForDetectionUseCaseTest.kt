package com.simprints.infra.uibase.camera.qrscan.usecase

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.*
import com.simprints.infra.uibase.camera.qrscan.QrCodeAnalyzer
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class CropBitmapAreaForDetectionUseCaseTest {

    @MockK
    lateinit var rotateIfNeeded: RotateIfNeededUseCase

    @MockK
    lateinit var mapCropRectToImageSpace: MapCropRectToImageSpaceUseCase

    @MockK
    lateinit var cropBitmapToRect: CropBitmapToRectUseCase

    @MockK
    lateinit var originalBitmap: Bitmap

    @MockK
    lateinit var rotatedBitmap: Bitmap

    @MockK
    lateinit var croppedBitmap: Bitmap

    private lateinit var useCase: CropBitmapAreaForDetectionUseCase

    private val cropConfig = QrCodeAnalyzer.CropConfig(
        rect = Rect(0, 0, 10, 10),
        orientation = 0,
        rootViewWidth = 100,
        rootViewHeight = 100
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { rotatedBitmap.width } returns 50
        every { rotatedBitmap.height } returns 50

        useCase = CropBitmapAreaForDetectionUseCase(
            rotateIfNeeded,
            mapCropRectToImageSpace,
            cropBitmapToRect
        )
    }

    @Test
    fun `returns cropped bitmap when rect is inside bounds`() {
        val mappedRect = Rect(5, 5, 15, 15)

        every { rotateIfNeeded(originalBitmap, 0) } returns rotatedBitmap
        every { mapCropRectToImageSpace(any(), any(), any(), any(), any()) } returns mappedRect
        every { cropBitmapToRect(rotatedBitmap, mappedRect) } returns croppedBitmap

        val result = useCase(originalBitmap, cropConfig)

        assertThat(result).isEqualTo(croppedBitmap)
        verify { rotateIfNeeded(bitmap = originalBitmap, orientation = 0) }
        verify {
            mapCropRectToImageSpace(
                cropRectInRoot = cropConfig.rect,
                rootWidth = 100,
                rootHeight = 100,
                imageWidth = 50,
                imageHeight = 50
            )
        }
        verify { cropBitmapToRect(source = rotatedBitmap, cropRect = mappedRect) }
    }

    @Test
    fun `returns original bitmap when rect is out of bounds`() {
        val mappedRect = Rect(-5, 5, 15, 15)

        every { rotateIfNeeded(originalBitmap, 0) } returns rotatedBitmap
        every { mapCropRectToImageSpace(any(), any(), any(), any(), any()) } returns mappedRect

        val result = useCase(originalBitmap, cropConfig)

        assertThat(result).isEqualTo(originalBitmap)
        verify { rotateIfNeeded(bitmap = originalBitmap, orientation = 0) }
        verify {
            mapCropRectToImageSpace(
                cropRectInRoot = cropConfig.rect,
                rootWidth = 100,
                rootHeight = 100,
                imageWidth = 50,
                imageHeight = 50
            )
        }
        verify(exactly = 0) { cropBitmapToRect(any(), any()) }
    }
}
