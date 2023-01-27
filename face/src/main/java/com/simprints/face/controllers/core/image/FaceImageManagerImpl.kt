package com.simprints.face.controllers.core.image

import com.simprints.core.DispatcherIO
import com.simprints.eventsystem.event.EventRepository
import com.simprints.face.data.moduleapi.face.responses.entities.Path
import com.simprints.face.data.moduleapi.face.responses.entities.SecuredImageRef
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.simprints.infra.images.model.Path as CorePath

class FaceImageManagerImpl @Inject constructor(
    private val coreImageRepository: ImageRepository,
    private val coreEventRepository: EventRepository,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) : FaceImageManager {

    override suspend fun save(imageBytes: ByteArray, captureEventId: String): SecuredImageRef? = withContext(dispatcher) {
        determinePath(captureEventId)?.let { path ->
            Simber.d("Saving face image ${path.compose()}")
            val currentSession = coreEventRepository.getCurrentCaptureSessionEvent()
            val projectId = currentSession.payload.projectId
            val securedImageRef = coreImageRepository.storeImageSecurely(imageBytes, projectId, path)

            if (securedImageRef != null) {
                SecuredImageRef(securedImageRef.relativePath.toDomain())
            } else {
                Simber.e("Saving image failed for captureId $captureEventId")
                null
            }
        }
    }

    private suspend fun determinePath(captureEventId: String): CorePath? =
        try {
            val currentSession = coreEventRepository.getCurrentCaptureSessionEvent()
            val sessionId = currentSession.id
            CorePath(
                arrayOf(
                    SESSIONS_PATH,
                    sessionId,
                    FACES_PATH,
                    "$captureEventId.jpg"
                )
            )
        } catch (t: Throwable) {
            Simber.e(t)
            null
        }

    private fun CorePath.toDomain(): Path =
        Path(this.parts)

    companion object {
        const val SESSIONS_PATH = "sessions"
        const val FACES_PATH = "faces"
    }
}
