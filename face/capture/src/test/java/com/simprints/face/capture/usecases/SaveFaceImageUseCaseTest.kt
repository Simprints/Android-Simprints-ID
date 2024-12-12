package com.simprints.face.capture.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.session.SessionEventRepository
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

class SaveFaceImageUseCaseTest {
    @MockK
    lateinit var imageRepo: ImageRepository

    @MockK
    lateinit var eventRepo: SessionEventRepository

    private lateinit var useCase: SaveFaceImageUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = SaveFaceImageUseCase(imageRepo, eventRepo)
    }

    @Test
    fun `save image should call the event and image repos`() = runTest {
        coEvery { eventRepo.getCurrentSessionScope() } returns mockk {
            every { projectId } returns "projectId"
            every { id } returns "sessionId"
        }

        val expectedPath = Path(
            arrayOf(
                "sessions",
                "sessionId",
                "faces",
                "captureEventId.jpg",
            ),
        )
        coEvery {
            imageRepo.storeImageSecurely(any(), "projectId", any())
        } returns SecuredImageRef(expectedPath)

        val imageBytes = byteArrayOf()
        val captureEventId = "captureEventId"

        assertThat(useCase.invoke(imageBytes, captureEventId)).isNotNull()

        coVerify {
            imageRepo.storeImageSecurely(
                withArg {
                    assert(it.isEmpty())
                },
                "projectId",
                withArg {
                    assert(expectedPath.compose().contains(it.compose()))
                },
            )
        }
    }

    @Test
    fun `returns null when no current session event`() = runTest {
        coEvery { eventRepo.getCurrentSessionScope() } throws Exception("no session")

        val imageBytes = byteArrayOf()
        val captureEventId = "captureEventId"

        assertThat(useCase.invoke(imageBytes, captureEventId)).isNull()
    }

    @Test
    fun `returns null when image is not saved`() = runTest {
        coEvery { eventRepo.getCurrentSessionScope() } returns mockk {
            every { projectId } returns "projectId"
            every { id } returns "sessionId"
        }
        coEvery {
            imageRepo.storeImageSecurely(any(), "projectId", any())
        } returns null

        val imageBytes = byteArrayOf()
        val captureEventId = "captureEventId"

        assertThat(useCase.invoke(imageBytes, captureEventId)).isNull()
        coVerify { imageRepo.storeImageSecurely(any(), "projectId", any()) }
    }
}
