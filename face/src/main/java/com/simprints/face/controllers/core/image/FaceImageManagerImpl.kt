package com.simprints.face.controllers.core.image

import com.simprints.eventsystem.event.EventRepository
import com.simprints.face.data.moduleapi.face.responses.entities.Path
import com.simprints.face.data.moduleapi.face.responses.entities.SecuredImageRef
import com.simprints.infra.logging.Simber
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.images.model.Path as CorePath

class FaceImageManagerImpl(private val coreImageRepository: ImageRepository,
                           private val coreEventRepository: EventRepository
) : FaceImageManager {

    override suspend fun save(imageBytes: ByteArray, captureEventId: String): SecuredImageRef? =
        determinePath(captureEventId)?.let { path ->
            Simber.d("Saving face image ${path.compose()}")
            val securedImageRef = coreImageRepository.storeImageSecurely(imageBytes, path)

            return if (securedImageRef != null) {
                SecuredImageRef(securedImageRef.relativePath.toDomain())
            } else {
                Simber.e("Saving image failed for captureId $captureEventId")
                null
            }
        }

    private suspend fun determinePath(captureEventId: String): CorePath? =
        try {
            val currentSession = coreEventRepository.getCurrentCaptureSessionEvent()

            val projectId = currentSession.payload.projectId
            val sessionId = currentSession.id
            CorePath(arrayOf(
                PROJECTS_PATH, projectId, SESSIONS_PATH, sessionId, FACES_PATH, "$captureEventId.jpg"
            ))
        } catch (t: Throwable) {
            Simber.e(t)
            null
        }

    private fun CorePath.toDomain(): Path =
        Path(this.parts)

    companion object {
        const val PROJECTS_PATH = "projects"
        const val SESSIONS_PATH = "sessions"
        const val FACES_PATH = "faces"
    }
}
