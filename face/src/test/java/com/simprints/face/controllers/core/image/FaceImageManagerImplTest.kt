package com.simprints.face.controllers.core.image

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.EventRepository
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.images.model.Path
import com.simprints.infra.images.model.SecuredImageRef
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FaceImageManagerImplTest {

    @MockK
    lateinit var imageRepo: ImageRepository

    @MockK
    lateinit var eventRepo: EventRepository

    private lateinit var faceImageManagerImpl: FaceImageManagerImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        faceImageManagerImpl = FaceImageManagerImpl(imageRepo, eventRepo)
    }

    @Test
    fun `save image should call the event and image repos`() = runTest {
        coEvery { eventRepo.getCurrentCaptureSessionEvent() } returns mockk {
            every { payload.projectId } returns "projectId"
            every { id } returns "sessionId"
        }

        val expectedPath = Path(
            arrayOf(
                "sessions", "sessionId", "faces", "captureEventId.jpg"
            )
        )
        coEvery {
            imageRepo.storeImageSecurely(any(), "projectId", any())
        } returns SecuredImageRef(expectedPath)


        val imageBytes = byteArrayOf()
        val captureEventId = "captureEventId"

        assertThat(faceImageManagerImpl.save(imageBytes, captureEventId)).isNotNull()

        coVerify {
            imageRepo.storeImageSecurely(
                withArg {
                    assert(it.isEmpty())
                },
                "projectId",
                withArg {
                    assert(expectedPath.compose().contains(it.compose()))
                })
        }
    }

    @Test
    fun `returns null when no current session event`() = runTest {
        coEvery { eventRepo.getCurrentCaptureSessionEvent() } throws Exception("no session")

        val imageBytes = byteArrayOf()
        val captureEventId = "captureEventId"

        assertThat(faceImageManagerImpl.save(imageBytes, captureEventId)).isNull()
    }

    @Test
    fun `returns null when image is not saved`() = runTest {
        coEvery { eventRepo.getCurrentCaptureSessionEvent() } returns mockk {
            every { payload.projectId } returns "projectId"
            every { id } returns "sessionId"
        }
        coEvery {
            imageRepo.storeImageSecurely(any(), "projectId", any())
        } returns null

        val imageBytes = byteArrayOf()
        val captureEventId = "captureEventId"

        assertThat(faceImageManagerImpl.save(imageBytes, captureEventId)).isNull()
        coVerify { imageRepo.storeImageSecurely(any(), "projectId", any()) }
    }

}
