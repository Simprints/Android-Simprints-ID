package com.simprints.infra.uibase.camera.qrscan

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.google.common.truth.*
import com.google.mlkit.vision.common.InputImage
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.uibase.camera.qrscan.usecase.CropBitmapAreaForDetectionUseCase
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class QrCodeAnalyzerTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var qrCodeDetector: QrCodeDetector

    @MockK
    lateinit var imageProxy: ImageProxy

    private lateinit var qrCodeProducer: QrCodeAnalyzer
    private lateinit var cropUseCase: CropBitmapAreaForDetectionUseCase
    private lateinit var bitmap: Bitmap

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        bitmap = mockk<Bitmap>()
        cropUseCase = mockk<CropBitmapAreaForDetectionUseCase>(relaxed = true)
    }

    private fun initQrCodeProducer(cropConfig: QrCodeAnalyzer.CropConfig?) {
        qrCodeProducer = QrCodeAnalyzer(
            qrCodeDetectorFactory = mockk<QrCodeDetector.Factory> {
                every { create(any()) } returns qrCodeDetector
            },
            bgDispatcher = testCoroutineRule.testCoroutineDispatcher,
            cropConfig = cropConfig,
            crashReportTag = LoggingConstants.CrashReportTag.LOGIN,
            cropBitmapAreaForDetectionUseCase = cropUseCase
        )
    }

    @Test
    fun `should not trigger detector when no images obtained`() {
        initQrCodeProducer(cropConfig = null)
        every { imageProxy.image } returns null
        qrCodeProducer.analyze(imageProxy)

        coVerify(exactly = 0) { qrCodeDetector.detectInImage(any<RawImage>()) }
    }

    @Test
    fun `should send RQ code value to flow`() = runTest {
        initQrCodeProducer(cropConfig = null)
        every { imageProxy.image } returns mockk()
        coEvery { qrCodeDetector.detectInImage(any<RawImage>()) } returns "mock_value"

        qrCodeProducer.analyze(imageProxy)
        val code = qrCodeProducer.scannedCode.first()

        Truth.assertThat(code).isEqualTo("mock_value")
    }

    @Test
    fun `should close image proxy with unsuccessful scan`() {
        initQrCodeProducer(cropConfig = null)
        coEvery { qrCodeDetector.detectInImage(any<RawImage>()) } throws Throwable()

        qrCodeProducer.analyze(imageProxy)

        verify { imageProxy.close() }
    }

    @Test
    fun `should emit code when cropConfig is provided`() = runTest {
        val cropConfig = mockk<QrCodeAnalyzer.CropConfig>()
        val expectedQrValue = "expectedQrValue"
        initQrCodeProducer(cropConfig)

        mockkStatic(InputImage::class)
        every { InputImage.fromBitmap(any(), any()) } returns mockk(relaxed = true)
        every { imageProxy.image } returns mockk(relaxed = true)
        every { imageProxy.toBitmap() } returns bitmap
        every { cropUseCase.invoke(bitmap, cropConfig) } returns bitmap
        coEvery { qrCodeDetector.detectInImage(any<InputImage>()) } returns expectedQrValue

        qrCodeProducer.analyze(imageProxy)
        val scannedCode = qrCodeProducer.scannedCode.first()
        Truth.assertThat(scannedCode).isEqualTo(expectedQrValue)
    }

}
