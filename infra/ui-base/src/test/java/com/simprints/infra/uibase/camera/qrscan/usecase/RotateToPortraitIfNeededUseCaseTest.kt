package com.simprints.infra.uibase.camera.qrscan.usecase

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.test.ext.junit.runners.*
import com.google.common.truth.Truth.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class RotateToPortraitIfNeededUseCaseTest {

    @MockK
    lateinit var landscapeBitmap: Bitmap

    @MockK
    lateinit var portraitBitmap: Bitmap

    @MockK
    lateinit var rotatedBitmap: Bitmap

    private lateinit var useCase: RotateToPortraitIfNeededUseCase

    private val w800 = 800
    private val h600 = 600
    private val w600 = 600
    private val h800 = 800

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { landscapeBitmap.width } returns w800
        every { landscapeBitmap.height } returns h600
        every { portraitBitmap.width } returns w600
        every { portraitBitmap.height } returns h800

        useCase = RotateToPortraitIfNeededUseCase()
    }

    @Test
    fun `rotates landscape bitmap when device is in portrait orientation`() {
        mockkStatic(Bitmap::class)
        every {
            Bitmap.createBitmap(
                landscapeBitmap,
                0,
                0,
                w800,
                h600,
                any<Matrix>(),
                true
            )
        } returns rotatedBitmap

        val result = useCase(landscapeBitmap, Configuration.ORIENTATION_PORTRAIT)

        assertThat(result).isEqualTo(rotatedBitmap)
        verify {
            Bitmap.createBitmap(
                landscapeBitmap,
                0,
                0,
                w800,
                h600,
                any<Matrix>(),
                true
            )
        }
        unmockkStatic(Bitmap::class)
    }

    @Test
    fun `returns original bitmap when landscape bitmap and device is in landscape orientation`() {
        runUseCaseTest(source = landscapeBitmap, orientation = Configuration.ORIENTATION_LANDSCAPE, expected = landscapeBitmap)
    }

    @Test
    fun `returns original bitmap when portrait bitmap and device is in portrait orientation`() {
        runUseCaseTest(source = portraitBitmap, orientation = Configuration.ORIENTATION_PORTRAIT, expected = portraitBitmap)
    }

    @Test
    fun `returns original bitmap when portrait bitmap and device is in landscape orientation`() {
        runUseCaseTest(source = portraitBitmap, orientation = Configuration.ORIENTATION_LANDSCAPE, expected = portraitBitmap)
    }

    @Test
    fun `returns original bitmap when device orientation is undefined`() {
        runUseCaseTest(source = landscapeBitmap, orientation = Configuration.ORIENTATION_UNDEFINED, expected = landscapeBitmap)
    }

    @Test
    fun `returns original bitmap when bitmap is square and device is in portrait orientation`() {
        val squareBitmap = mockk<Bitmap>()
        every { squareBitmap.width } returns w600
        every { squareBitmap.height } returns w600

        runUseCaseTest(source = squareBitmap, orientation = Configuration.ORIENTATION_PORTRAIT, expected = squareBitmap)
    }

    private fun runUseCaseTest(source: Bitmap, orientation: Int, expected: Bitmap) {
        val result = useCase(source, orientation)
        assertThat(result).isEqualTo(expected)
    }
}
