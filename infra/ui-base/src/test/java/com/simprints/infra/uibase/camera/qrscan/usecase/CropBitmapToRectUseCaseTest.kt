package com.simprints.infra.uibase.camera.qrscan.usecase

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class CropBitmapToRectUseCaseTest {

    @MockK
    lateinit var sourceBitmap: Bitmap

    @MockK
    lateinit var croppedBitmap: Bitmap

    private lateinit var useCase: CropBitmapToRectUseCase

    private val w800 = 800
    private val h600 = 600
    private val x100 = 100
    private val y100 = 100
    private val w200 = 200
    private val h150 = 150
    private val x0 = 0
    private val y0 = 0
    private val x50 = 50
    private val y50 = 50
    private val w100 = 100
    private val h100 = 100
    private val x900 = 900
    private val y700 = 700
    private val x750 = 750
    private val y550 = 550

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { sourceBitmap.width } returns w800
        every { sourceBitmap.height } returns h600

        useCase = CropBitmapToRectUseCase()
    }

    @Test
    fun `crops bitmap with rect inside bounds`() {
        val cropRect = Rect(x100, y100, x100 + w200, y100 + h150)
        runUseCaseTest(cropRect, x100, y100, w200, h150)
    }

    @Test
    fun `crops bitmap with rect at origin`() {
        val cropRect = Rect(x0, y0, w100, h100)
        runUseCaseTest(cropRect, x0, y0, w100, h100)
    }

    @Test
    fun `coerces negative left coordinate to zero`() {
        val cropRect = Rect(-x50, y100, -x50 + w100, y100 + h100)
        runUseCaseTest(cropRect, x0, y100, w100, h100)
    }

    @Test
    fun `coerces negative top coordinate to zero`() {
        val cropRect = Rect(x100, -y50, x100 + w100, -y50 + h100)
        runUseCaseTest(cropRect, x100, x0, w100, h100)
    }

    @Test
    fun `coerces left coordinate exceeding bitmap width`() {
        val cropRect = Rect(x900, y100, x900 + w100, y100 + h100)
        runUseCaseTest(cropRect, w800, y100, x0, h100)
    }

    @Test
    fun `coerces top coordinate exceeding bitmap height`() {
        val cropRect = Rect(x100, y700, x100 + w100, y700 + h100)
        runUseCaseTest(cropRect, x100, h600, w100, x0)
    }

    @Test
    fun `limits width when crop extends beyond bitmap width`() {
        val cropRect = Rect(x750, y100, x750 + w200, y100 + h100)
        val expectedWidth = w800 - x750
        runUseCaseTest(cropRect, x750, y100, expectedWidth, h100)
    }

    @Test
    fun `limits height when crop extends beyond bitmap height`() {
        val cropRect = Rect(x100, y550, x100 + w100, y550 + h150)
        val expectedHeight = h600 - y550
        runUseCaseTest(cropRect, x100, y550, w100, expectedHeight)
    }

    @Test
    fun `handles rect completely outside bounds with adjusted coordinates`() {
        val cropRect = Rect(x900, y700, x900 + w100, y700 + h100)
        runUseCaseTest(cropRect, w800, h600, x0, x0)
    }

    private fun runUseCaseTest(
        cropRect: Rect,
        expectedLeft: Int,
        expectedTop: Int,
        expectedWidth: Int,
        expectedHeight: Int
    ) {
        mockkStatic(Bitmap::class)

        every {
            Bitmap.createBitmap(
                sourceBitmap,
                expectedLeft,
                expectedTop,
                expectedWidth,
                expectedHeight
            )
        } returns croppedBitmap

        val result = useCase(sourceBitmap, cropRect)

        assertThat(result).isEqualTo(croppedBitmap)
        verify {
            Bitmap.createBitmap(
                sourceBitmap,
                expectedLeft,
                expectedTop,
                expectedWidth,
                expectedHeight
            )
        }
        unmockkStatic(Bitmap::class)
    }
}
