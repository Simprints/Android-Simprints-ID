package com.simprints.fingerprint.capture.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.capture.state.CaptureState
import com.simprints.fingerprint.capture.state.ScanResult
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.Vero2Configuration
import com.simprints.infra.events.EventRepository
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
    lateinit var eventRepo: EventRepository

    @MockK
    lateinit var configuration: FingerprintConfiguration


    private lateinit var useCase: SaveImageUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { configuration.vero2?.imageSavingStrategy } returns Vero2Configuration.ImageSavingStrategy.EAGER

        useCase = SaveImageUseCase(imageRepo, eventRepo)
    }

    @Test
    fun `Returns null if no scan image`() = runTest {
        val result = useCase.invoke(
            configuration,
            "captureEventId",
            createCollectedStub(null)
        )
        assertThat(result).isNull()
    }

    @Test
    fun `Returns null if no capture event id`() = runTest {
        val result = useCase.invoke(
            configuration,
            null,
            createCollectedStub(byteArrayOf())
        )
        assertThat(result).isNull()
    }

    @Test
    fun `Save image should call the event and image repos`() = runTest {
        coEvery { eventRepo.getCurrentCaptureSessionEvent() } returns mockk {
            every { payload.projectId } returns "projectId"
            every { id } returns "sessionId"
        }

        val expectedPath = Path(arrayOf(
            "sessions",
            "sessionId",
            "fingerprints",
            "captureEventId.wsq"
        ))
        coEvery {
            imageRepo.storeImageSecurely(any(), "projectId", any())
        } returns SecuredImageRef(expectedPath)

        assertThat(useCase.invoke(
            configuration,
            "captureEventId",
            createCollectedStub(byteArrayOf())
        )).isNotNull()

        coVerify {
            imageRepo.storeImageSecurely(
                withArg { assert(it.isEmpty()) },
                "projectId",
                withArg {
                    assert(expectedPath.compose().contains(it.compose()))
                }
            )
        }
    }

    @Test
    fun `Returns null when no current session event`() = runTest {
        coEvery { eventRepo.getCurrentCaptureSessionEvent() } throws Exception("no session")

        assertThat(useCase.invoke(
            configuration,
            "captureEventId",
            createCollectedStub(byteArrayOf())
        )).isNull()
    }

    @Test
    fun `Returns null when image is not saved`() = runTest {
        coEvery { eventRepo.getCurrentCaptureSessionEvent() } returns mockk {
            every { payload.projectId } returns "projectId"
            every { id } returns "sessionId"
        }
        coEvery {
            imageRepo.storeImageSecurely(any(), "projectId", any())
        } returns null

        assertThat(useCase.invoke(
            configuration,
            "captureEventId",
            createCollectedStub(byteArrayOf())
        )).isNull()

        coVerify { imageRepo.storeImageSecurely(any(), "projectId", any()) }
    }

    private fun createCollectedStub(image: ByteArray?) = CaptureState.Collected(
        ScanResult(
            0,
            byteArrayOf(),
            "format",
            image,
            10,
        ),
        0
    )
}
