package com.simprints.fingerprint.capture.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.fingerprint.capture.state.CaptureState
import com.simprints.fingerprint.capture.state.ScanResult
import com.simprints.infra.config.store.models.Vero2Configuration
import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.images.model.Path
import com.simprints.infra.images.model.SecuredImageRef
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SaveImageUseCaseTest {

    @MockK
    lateinit var imageRepo: ImageRepository

    @MockK
    lateinit var eventRepo: SessionEventRepository

    @MockK
    lateinit var vero2Configuration: Vero2Configuration

    private lateinit var useCase: SaveImageUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { vero2Configuration.imageSavingStrategy } returns Vero2Configuration.ImageSavingStrategy.EAGER
        every { vero2Configuration.captureStrategy } returns Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1300_DPI

        useCase = SaveImageUseCase(imageRepo, eventRepo)
    }

    @Test
    fun `Returns null if no scan image`() = runTest {
        val result = useCase.invoke(
            vero2Configuration,
            IFingerIdentifier.LEFT_3RD_FINGER,
            "captureEventId",
            createCollectedStub(null)
        )
        assertThat(result).isNull()
    }

    @Test
    fun `Returns null if no capture event id`() = runTest {
        val result = useCase.invoke(
            vero2Configuration,
            IFingerIdentifier.LEFT_3RD_FINGER,
            null,
            createCollectedStub(byteArrayOf())
        )
        assertThat(result).isNull()
    }

    @Test
    fun `Save image should call the event and image repos`() = runTest {
        coEvery { eventRepo.getCurrentSessionScope() } returns mockk {
            every { projectId } returns "projectId"
            every { id } returns "sessionId"
        }

        val expectedPath = Path(arrayOf(
            "sessions",
            "sessionId",
            "fingerprints",
            "captureEventId.wsq"
        ))
        coEvery {
            imageRepo.storeImageSecurely(any(), "projectId", any(), any())
        } returns SecuredImageRef(expectedPath)

        assertThat(useCase.invoke(
            vero2Configuration,
            IFingerIdentifier.LEFT_3RD_FINGER,
            "captureEventId",
            createCollectedStub(byteArrayOf())
        )).isNotNull()

        coVerify {
            imageRepo.storeImageSecurely(
                withArg { assert(it.isEmpty()) },
                "projectId",
                withArg {
                    assert(expectedPath.compose().contains(it.compose()))
                },
                any()
            )
        }
    }

    @Test
    fun `Returns null when no current session event`() = runTest {
        coEvery { eventRepo.getCurrentSessionScope() } throws Exception("no session")

        assertThat(useCase.invoke(
            vero2Configuration,
            IFingerIdentifier.LEFT_3RD_FINGER,
            "captureEventId",
            createCollectedStub(byteArrayOf())
        )).isNull()
    }

    @Test
    fun `Returns null when image is not saved`() = runTest {
        coEvery { eventRepo.getCurrentSessionScope() } returns mockk {
            every { projectId } returns "projectId"
            every { id } returns "sessionId"
        }
        coEvery {
            imageRepo.storeImageSecurely(any(), "projectId", any(), any())
        } returns null

        assertThat(useCase.invoke(
            vero2Configuration,
            IFingerIdentifier.LEFT_3RD_FINGER,
            "captureEventId",
            createCollectedStub(byteArrayOf())
        )).isNull()

        coVerify { imageRepo.storeImageSecurely(any(), "projectId", any(), any()) }
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
