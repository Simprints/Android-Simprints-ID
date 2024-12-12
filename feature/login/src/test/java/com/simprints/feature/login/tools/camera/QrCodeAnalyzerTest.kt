package com.simprints.feature.login.tools.camera

import androidx.camera.core.ImageProxy
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class QrCodeAnalyzerTest {
    @MockK
    lateinit var mockQrCodeDetector: QrCodeDetector

    @MockK
    lateinit var mockImageProxy: ImageProxy

    private lateinit var qrCodeProducer: QrCodeAnalyzer

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        qrCodeProducer = QrCodeAnalyzer(mockQrCodeDetector, UnconfinedTestDispatcher())
    }

    @Test
    fun `should not trigger detector when no images obtained`() {
        every { mockImageProxy.image } returns null
        qrCodeProducer.analyze(mockImageProxy)

        coVerify(exactly = 0) { mockQrCodeDetector.detectInImage(any()) }
    }

    @Test
    fun `should send RQ code value to flow`() = runTest {
        every { mockImageProxy.image } returns mockk()
        coEvery { mockQrCodeDetector.detectInImage(any()) } returns "mock_value"

        qrCodeProducer.analyze(mockImageProxy)
        val code = qrCodeProducer.scannedCode.first()

        assertThat(code).isEqualTo("mock_value")
    }

    @Test
    fun `should close image proxy with unsuccessful scan`() {
        coEvery { mockQrCodeDetector.detectInImage(any()) } throws Throwable()

        qrCodeProducer.analyze(mockImageProxy)

        verify { mockImageProxy.close() }
    }
}
