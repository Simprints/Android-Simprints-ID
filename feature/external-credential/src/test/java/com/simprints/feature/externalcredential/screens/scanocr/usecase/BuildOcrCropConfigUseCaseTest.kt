package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Rect
import android.view.View
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrCropConfig
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

internal class BuildOcrCropConfigUseCaseTest {
    private lateinit var getBoundsRelativeToParentUseCase: GetBoundsRelativeToParentUseCase
    private lateinit var useCase: BuildOcrCropConfigUseCase

    private val mockCameraPreview = mockk<View>()
    private val mockDocumentScannerArea = mockk<View>()
    private val mockRect = mockk<Rect>()
    private val width800 = 800
    private val height600 = 600

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        getBoundsRelativeToParentUseCase = mockk()
        useCase = BuildOcrCropConfigUseCase(getBoundsRelativeToParentUseCase)

        every { mockCameraPreview.width } returns width800
        every { mockCameraPreview.height } returns height600
        every { getBoundsRelativeToParentUseCase(mockCameraPreview, mockDocumentScannerArea) } returns mockRect
    }

    private fun runUseCaseTest(rotationDegrees: Int = 0) = useCase(rotationDegrees, mockCameraPreview, mockDocumentScannerArea)

    @Test
    fun `creates config with correct rotation degrees`() {
        val rotationDegrees = 90
        val result = runUseCaseTest(rotationDegrees)
        assertThat(result.rotationDegrees).isEqualTo(rotationDegrees)
    }

    @Test
    fun `creates config with correct preview dimensions`() {
        val result = runUseCaseTest()
        assertThat(result.previewViewWidth).isEqualTo(800)
        assertThat(result.previewViewHeight).isEqualTo(600)
    }

    @Test
    fun `creates config with cutout rect from bounds use case`() {
        val result = runUseCaseTest()
        assertThat(result.cutoutRect).isEqualTo(mockRect)
    }

    @Test
    fun `creates complete config with all parameters`() {
        val rotationDegrees = 180
        val result = runUseCaseTest(rotationDegrees)
        assertThat(result).isEqualTo(
            OcrCropConfig(
                rotationDegrees = rotationDegrees,
                cutoutRect = mockRect,
                previewViewWidth = width800,
                previewViewHeight = height600,
            ),
        )
    }

    @Test
    fun `handles zero rotation degrees`() {
        val result = runUseCaseTest(0)
        assertThat(result.rotationDegrees).isEqualTo(0)
    }

    @Test
    fun `handles different preview dimensions`() {
        val expectedWith = 1920
        val expectedHeight = 1080
        every { mockCameraPreview.width } returns expectedWith
        every { mockCameraPreview.height } returns expectedHeight

        val result = runUseCaseTest()

        assertThat(result.previewViewWidth).isEqualTo(expectedWith)
        assertThat(result.previewViewHeight).isEqualTo(expectedHeight)
    }
}
