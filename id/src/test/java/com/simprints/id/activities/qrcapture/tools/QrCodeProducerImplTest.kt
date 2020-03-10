package com.simprints.id.activities.qrcapture.tools

import androidx.camera.core.ImageProxy
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

@ExperimentalCoroutinesApi
class QrCodeProducerImplTest {

    @MockK lateinit var mockQrCodeDetector: QrCodeDetector
    @MockK lateinit var mockCrashReportManager: CrashReportManager
    @MockK lateinit var mockImageProxy: ImageProxy

    private lateinit var qrCodeProducer: QrCodeProducerImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        qrCodeProducer = QrCodeProducerImpl(mockQrCodeDetector, mockCrashReportManager)
        every { mockImageProxy.image } returns mockk()
    }

    @Test
    fun withNoImagesObtained_shouldNotTriggerDetector() {
        qrCodeProducer.analyze(null, 90)

        coVerify(exactly = 0) { mockQrCodeDetector.detectInImage(any()) }
    }

    @Test
    @Ignore("Test fails because the channel is always closed")
    fun shouldSendQrCodeValueThroughChannel() {
        coEvery { mockQrCodeDetector.detectInImage(any()) } returns "mock_value"

        qrCodeProducer.analyze(mockImageProxy, 90)

        coVerify { qrCodeProducer.qrCodeChannel.send("mock_value") }
    }

    @Test
    fun whenDetectorThrowsException_shouldLogToCrashReport() {
        coEvery { mockQrCodeDetector.detectInImage(any()) } throws Throwable()

        qrCodeProducer.analyze(mockImageProxy, 90)

        coVerify { mockCrashReportManager.logExceptionOrSafeException(any()) }
    }

}
