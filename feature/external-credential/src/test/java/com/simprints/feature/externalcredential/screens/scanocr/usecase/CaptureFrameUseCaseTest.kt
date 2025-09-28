package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import androidx.camera.core.ImageInfo
import androidx.camera.core.ImageProxy
import com.google.common.truth.Truth.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class CaptureFrameUseCaseTest {

    private lateinit var useCase: CaptureFrameUseCase

    @Before
    fun setUp() {
        useCase = CaptureFrameUseCase()
    }

    @Test
    fun `creates pair with correct bitmap and image info`() = runTest {
        val mockImageProxy = mockk<ImageProxy>()
        val mockBitmap = mockk<Bitmap>()
        val mockImageInfo = mockk<ImageInfo>()

        every { mockImageProxy.toBitmap() } returns mockBitmap
        every { mockImageProxy.imageInfo } returns mockImageInfo

        val result = useCase(mockImageProxy)

        assertThat(result).isEqualTo(mockBitmap to mockImageInfo)
    }
}
