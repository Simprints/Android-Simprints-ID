package com.simprints.fingerprint.capture.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.image.Path
import com.simprints.core.domain.image.SecuredImageRef
import com.simprints.core.domain.modality.Modality
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.fingerprint.capture.state.CaptureState
import com.simprints.fingerprint.capture.state.ScanResult
import com.simprints.fingerprint.infra.scanner.v2.scanner.ScannerInfo
import com.simprints.infra.config.store.models.Vero2Configuration
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.images.ImageRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SaveFingerprintSampleUseCaseTest {
    @MockK
    lateinit var imageRepo: ImageRepository

    @MockK
    lateinit var eventRepo: SessionEventRepository

    @MockK
    lateinit var vero2Configuration: Vero2Configuration

    private lateinit var useCase: SaveFingerprintSampleUseCase
    private lateinit var scannerInfo: ScannerInfo

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        scannerInfo = ScannerInfo()
        every { vero2Configuration.imageSavingStrategy } returns Vero2Configuration.ImageSavingStrategy.EAGER
        every { vero2Configuration.captureStrategy } returns Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1300_DPI

        useCase = SaveFingerprintSampleUseCase(imageRepo, eventRepo, scannerInfo)
    }

    @Test
    fun `Returns null if no scan image`() = runTest {
        val result = useCase.invoke(
            vero2Configuration,
            SampleIdentifier.LEFT_3RD_FINGER,
            "captureEventId",
            createCollectedStub(null),
        )
        assertThat(result).isNull()
    }

    @Test
    fun `Returns null if no capture event id`() = runTest {
        val result = useCase.invoke(
            vero2Configuration,
            SampleIdentifier.LEFT_3RD_FINGER,
            null,
            createCollectedStub(byteArrayOf()),
        )
        assertThat(result).isNull()
    }

    @Test
    fun `Save image should call the event and image repos with correct metadata`() = runTest {
        val scannerId = "scannerId"
        val un20SerialNumber = "un20SerialNumber"
        val expectedMetadata = mapOf(
            "finger" to SampleIdentifier.LEFT_3RD_FINGER.name,
            "dpi" to "1300",
            "scannerID" to scannerId,
            "un20SerialNumber" to un20SerialNumber,
        )
        coEvery { eventRepo.getCurrentSessionScope() } returns mockk {
            every { projectId } returns "projectId"
            every { id } returns "sessionId"
        }
        scannerInfo.setScannerId(scannerId)
        scannerInfo.setUn20SerialNumber(un20SerialNumber)

        val expectedPath = Path(
            arrayOf(
                "sessions",
                "sessionId",
                "fingerprints",
                "captureEventId.wsq",
            ),
        )
        coEvery {
            imageRepo.storeSample("projectId", "sessionId", any(), any(), any(), any(), any())
        } returns SecuredImageRef(expectedPath)

        assertThat(
            useCase.invoke(
                vero2Configuration,
                SampleIdentifier.LEFT_3RD_FINGER,
                "captureEventId",
                createCollectedStub(byteArrayOf()),
            ),
        ).isNotNull()

        coVerify { imageRepo.storeSample(any(), any(), any(), any(), any(), any(), eq(expectedMetadata)) }
    }

    @Test
    fun `Returns null when no current session event`() = runTest {
        coEvery { eventRepo.getCurrentSessionScope() } throws Exception("no session")

        assertThat(
            useCase.invoke(
                vero2Configuration,
                SampleIdentifier.LEFT_3RD_FINGER,
                "captureEventId",
                createCollectedStub(byteArrayOf()),
            ),
        ).isNull()
    }

    @Test
    fun `Returns null when image is not saved`() = runTest {
        coEvery { eventRepo.getCurrentSessionScope() } returns mockk {
            every { projectId } returns "projectId"
            every { id } returns "sessionId"
        }
        coEvery {
            imageRepo.storeSample(any(), any(), Modality.FINGERPRINT, any(), any(), any(), any())
        } returns null

        assertThat(
            useCase.invoke(
                vero2Configuration,
                SampleIdentifier.LEFT_3RD_FINGER,
                "captureEventId",
                createCollectedStub(byteArrayOf()),
            ),
        ).isNull()

        coVerify { imageRepo.storeSample(any(), any(), any(), any(), any(), any(), any()) }
    }

    private fun createCollectedStub(image: ByteArray?) = CaptureState.ScanProcess.Collected(
        numberOfBadScans = 0,
        numberOfNoFingerDetectedScans = 0,
        scanResult = ScanResult(
            0,
            byteArrayOf(),
            "format",
            image,
            10,
        ),
    )
}
