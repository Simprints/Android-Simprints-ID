package com.simprints.face.capture.screens.livefeedback


import android.graphics.Rect
import android.graphics.RectF
import android.util.Size
import androidx.camera.core.ImageProxy
import com.google.common.truth.Truth.assertThat
import com.simprints.face.capture.usecases.ImageProxyToBitmapUseCase
import io.mockk.CapturingSlot
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import org.junit.Before
import org.junit.Test

internal class FrameProcessorTest {

    @MockK
    private lateinit var imageToBitmap: ImageProxyToBitmapUseCase

    @MockK
    private lateinit var image: ImageProxy

    private lateinit var frameProcessor: FrameProcessor

    private lateinit var cropRectCapture: CapturingSlot<Rect>
    private val boxOnTheScreen = RectF(100f, 100f, 200f, 200f)

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        cropRectCapture = slot()
        every { imageToBitmap.invoke(any(), capture(cropRectCapture)) } returns mockk()

        frameProcessor = FrameProcessor(imageToBitmap)
    }


    @Test
    fun `test cropRotateFrame with portrait orientation and cameraWidth equal to screenWidth`() {
        // Given
        every { image.width } returns 1000
        every { image.height } returns 1000
        every { image.imageInfo.rotationDegrees } returns 0

        val screenWidth = 1000
        val screenHeight = 500

        frameProcessor.init(Size(screenWidth, screenHeight), boxOnTheScreen)
        // When
        frameProcessor.cropRotateFrame(image)

        // Then
        assertThat(cropRectCapture.captured.toString())
            .isEqualTo(Rect(100, 100, 200, 200).toString())
    }

    @Test
    fun `test cropRotateFrame with flipped portrait orientation and cameraWidth equal to screenWidth`() {
        // Given
        every { image.width } returns 1000
        every { image.height } returns 1000
        every { image.imageInfo.rotationDegrees } returns 180

        val screenWidth = 1000
        val screenHeight = 500

        frameProcessor.init(Size(screenWidth, screenHeight), boxOnTheScreen)
        // When
        frameProcessor.cropRotateFrame(image)

        // Then
        assertThat(cropRectCapture.captured.toString())
            .isEqualTo(Rect(100, 100, 200, 200).toString())
    }

    @Test
    fun `test cropRotateFrame with landscape orientation and cameraWidth equal to screenWidth`() {
        // Given
        every { image.width } returns 1000
        every { image.height } returns 1000
        every { image.imageInfo.rotationDegrees } returns 90

        val screenWidth = 1000
        val screenHeight = 500

        frameProcessor.init(Size(screenWidth, screenHeight), boxOnTheScreen)
        // When
        frameProcessor.cropRotateFrame(image)

        // Then
        assertThat(cropRectCapture.captured.toString())
            .isEqualTo(Rect(200, 100, 100, 200).toString())
    }

    @Test
    fun `test cropRotateFrame with flipped landscape orientation and cameraWidth equal to screenWidth`() {
        // Given
        every { image.width } returns 1000
        every { image.height } returns 1000
        every { image.imageInfo.rotationDegrees } returns 270

        val screenWidth = 1000
        val screenHeight = 500

        frameProcessor.init(Size(screenWidth, screenHeight), boxOnTheScreen)
        // When
        frameProcessor.cropRotateFrame(image)

        // Then
        assertThat(cropRectCapture.captured.toString())
            .isEqualTo(Rect(200, 100, 100, 200).toString())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test cropRotateFrame with unsupported orientation and cameraWidth equal to screenWidth`() {
        // Given
        every { image.width } returns 1000
        every { image.height } returns 1000
        every { image.imageInfo.rotationDegrees } returns 10

        val screenWidth = 1000
        val screenHeight = 500

        frameProcessor.init(Size(screenWidth, screenHeight), boxOnTheScreen)
        // When
        frameProcessor.cropRotateFrame(image)
        // Then throw IllegalArgumentException

    }

    @Test
    fun `test cropRotateFrame with portrait orientation and cameraWidth greater than screenWidth`() {
        // Given
        every { image.width } returns 2000
        every { image.height } returns 1000
        every { image.imageInfo.rotationDegrees } returns 0

        val screenWidth = 1000
        val screenHeight = 500

        frameProcessor.init(Size(screenWidth, screenHeight), boxOnTheScreen)
        // When
        frameProcessor.cropRotateFrame(image)
        // Then
        assertThat(cropRectCapture.captured.toString())
            .isEqualTo(Rect(100, 100, 200, 200).toString())
    }

    @Test
    fun `test cropRotateFrame with landscape orientation and cameraWidth greater than screenWidth`() {
        // Given
        every { image.width } returns 2000
        every { image.height } returns 1000
        every { image.imageInfo.rotationDegrees } returns 90

        val screenWidth = 1000
        val screenHeight = 500

        frameProcessor.init(Size(screenWidth, screenHeight), boxOnTheScreen)
        // When
        frameProcessor.cropRotateFrame(image)
        // Then
        assertThat(cropRectCapture.captured.toString())
            .isEqualTo(Rect(100, 100, 200, 200).toString())
    }

}
