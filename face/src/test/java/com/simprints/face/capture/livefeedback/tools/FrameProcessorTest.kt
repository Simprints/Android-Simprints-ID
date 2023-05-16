package com.simprints.face.capture.livefeedback.tools


import android.graphics.Rect
import android.graphics.RectF
import android.util.Size
import androidx.camera.core.ImageProxy
import com.google.common.truth.Truth
import com.simprints.face.capture.toBitmap
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import org.junit.Before
import org.junit.Test

class FrameProcessorTest {


    private lateinit var cropRectCapture: CapturingSlot<Rect>
    private val boxOnTheScreen = RectF(100f, 100f, 200f, 200f)
    private val image: ImageProxy = mockk()

    @Before
    fun setUp() {
        mockkStatic("com.simprints.face.capture.BitmapExtKt")
        cropRectCapture = slot()
        every { image.toBitmap(capture(cropRectCapture)) } returns mockk()
    }


    @Test
    fun `test cropRotateFrame with portrait orientation and cameraWidth equal to screenWidth`() {
        // Given
        every { image.width } returns 1000
        every { image.height } returns 1000
        every { image.imageInfo.rotationDegrees } returns 0

        val screenWidth = 1000
        val screenHeight = 500
        val frameProcessor = FrameProcessor()
        frameProcessor.init(Size(screenWidth, screenHeight), boxOnTheScreen)
        // When
        frameProcessor.cropRotateFrame(image)

        // Then
        Truth.assertThat(Rect(100, 100, 200, 200).toString())
            .isEqualTo(cropRectCapture.captured.toString())
    }

    @Test
    fun `test cropRotateFrame with landscape orientation and cameraWidth equal to screenWidth`() {
        // Given
        every { image.width } returns 1000
        every { image.height } returns 1000
        every { image.imageInfo.rotationDegrees } returns 90

        val screenWidth = 1000
        val screenHeight = 500
        val frameProcessor = FrameProcessor()
        frameProcessor.init(Size(screenWidth, screenHeight), boxOnTheScreen)
        // When
        frameProcessor.cropRotateFrame(image)

        // Then
        Truth.assertThat(Rect(200, 100, 100, 200).toString())
            .isEqualTo(cropRectCapture.captured.toString())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test cropRotateFrame with unsupported orientation and cameraWidth equal to screenWidth`() {
        // Given
        every { image.width } returns 1000
        every { image.height } returns 1000
        every { image.imageInfo.rotationDegrees } returns 10

        val screenWidth = 1000
        val screenHeight = 500
        val frameProcessor = FrameProcessor()
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
        val frameProcessor = FrameProcessor()
        frameProcessor.init(Size(screenWidth, screenHeight), boxOnTheScreen)
        // When
        frameProcessor.cropRotateFrame(image)
        // Then
        Truth.assertThat(Rect(100, 100, 200, 200).toString())
            .isEqualTo(cropRectCapture.captured.toString())
    }

    @Test
    fun `test cropRotateFrame with landscape orientation and cameraWidth greater than screenWidth`() {
        // Given
        every { image.width } returns 2000
        every { image.height } returns 1000
        every { image.imageInfo.rotationDegrees } returns 90

        val screenWidth = 1000
        val screenHeight = 500
        val frameProcessor = FrameProcessor()
        frameProcessor.init(Size(screenWidth, screenHeight), boxOnTheScreen)
        // When
        frameProcessor.cropRotateFrame(image)
        // Then
        Truth.assertThat(Rect(100, 100, 200, 200).toString())
            .isEqualTo(cropRectCapture.captured.toString())
    }
}
