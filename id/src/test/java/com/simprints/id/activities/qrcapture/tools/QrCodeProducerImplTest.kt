package com.simprints.id.activities.qrcapture.tools

import androidx.camera.core.ImageProxy
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class QrCodeProducerImplTest {

    @MockK lateinit var mockQrCodeDetector: QrCodeDetector
    @MockK lateinit var mockImageProxy: ImageProxy
    @MockK lateinit var mockQrCodeChannel: Channel<String>

    private lateinit var qrCodeProducer: QrCodeProducerImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        qrCodeProducer = QrCodeProducerImpl(mockQrCodeDetector).apply {
            qrCodeChannel = mockQrCodeChannel
        }
    }

    @Test
    fun withNoImagesObtained_shouldNotTriggerDetector() {
        every { mockImageProxy.image } returns null
        qrCodeProducer.analyze(mockImageProxy)

        coVerify(exactly = 0) { mockQrCodeDetector.detectInImage(any()) }
    }

    @Test
    fun shouldSendQrCodeValueThroughChannel() {
        every { mockImageProxy.image } returns mockk()
        coEvery { mockQrCodeDetector.detectInImage(any()) } returns "mock_value"

        qrCodeProducer.analyze(mockImageProxy)

        coVerify { mockQrCodeChannel.send("mock_value") }
    }

    @Test
    fun withSuccessfulScan_shouldCloseImageProxy() {
        every { mockImageProxy.image } returns mockk()
        coEvery { mockQrCodeDetector.detectInImage(any()) } returns "mock_value"

        qrCodeProducer.analyze(mockImageProxy)

        verify { mockImageProxy.close() }
    }

    @Test
    fun withUnsuccessfulScan_shouldCloseImageProxy() {
        coEvery { mockQrCodeDetector.detectInImage(any()) } throws Throwable()

        qrCodeProducer.analyze(mockImageProxy)

        verify { mockImageProxy.close() }
    }

}
