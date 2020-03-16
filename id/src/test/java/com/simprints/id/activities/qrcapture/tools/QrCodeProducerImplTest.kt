package com.simprints.id.activities.qrcapture.tools

import androidx.camera.core.ImageProxy
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class QrCodeProducerImplTest {

    @MockK lateinit var mockQrCodeDetector: QrCodeDetector
    @MockK lateinit var mockCrashReportManager: CrashReportManager
    @MockK lateinit var mockImageProxy: ImageProxy
    @MockK lateinit var mockQrCodeChannel: Channel<String>

    private lateinit var qrCodeProducer: QrCodeProducerImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        qrCodeProducer = QrCodeProducerImpl(mockQrCodeDetector, mockCrashReportManager).apply {
            qrCodeChannel = mockQrCodeChannel
        }
        every { mockImageProxy.image } returns mockk()
    }

    @Test
    fun withNoImagesObtained_shouldNotTriggerDetector() {
        qrCodeProducer.analyze(null, 90)

        coVerify(exactly = 0) { mockQrCodeDetector.detectInImage(any()) }
    }

    @Test
    fun shouldSendQrCodeValueThroughChannel() {
        coEvery { mockQrCodeDetector.detectInImage(any()) } returns "mock_value"

        qrCodeProducer.analyze(mockImageProxy, 90)

        coVerify { mockQrCodeChannel.send("mock_value") }
    }

    @Test
    fun whenDetectorThrowsException_shouldLogToCrashReport() {
        coEvery { mockQrCodeDetector.detectInImage(any()) } throws Throwable()

        qrCodeProducer.analyze(mockImageProxy, 90)

        coVerify { mockCrashReportManager.logExceptionOrSafeException(any()) }
    }

}
