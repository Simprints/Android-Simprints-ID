package com.simprints.face.controllers.core.image

import com.simprints.eventsystem.event.EventRepository
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.images.model.Path
import com.simprints.infra.images.model.SecuredImageRef
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FaceImageManagerImplTest {

    @Test
    fun `save image should call the event and image repos`() = runTest {

        val expectedPath = Path("projects/projectId/sessions/sessionId/faces/captureEventId.jpg")

        val imageRepo = mockk<ImageRepository> {
            every { storeImageSecurely(any(), any()) } returns SecuredImageRef(expectedPath)
        }

        val eventRepo = mockk<EventRepository> {
            coEvery { getCurrentCaptureSessionEvent() } returns mockk() {
                every { payload.projectId } returns "projectId"
                every { id } returns "sessionId"
            }
        }

        val faceImageManagerImpl = FaceImageManagerImpl(imageRepo, eventRepo)

        val imageBytes = byteArrayOf()
        val captureEventId = "captureEventId"

        faceImageManagerImpl.save(imageBytes, captureEventId)

        verify {
            imageRepo.storeImageSecurely(
                withArg {
                    assert(it.isEmpty())
                },
                withArg {
                    assert(expectedPath.compose().contains(it.compose()))
                })
        }
    }

}
