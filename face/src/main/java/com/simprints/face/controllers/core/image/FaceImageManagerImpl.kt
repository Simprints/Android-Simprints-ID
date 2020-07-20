package com.simprints.face.controllers.core.image

import com.simprints.face.data.moduleapi.face.responses.entities.Path
import com.simprints.face.data.moduleapi.face.responses.entities.SecuredImageRef
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent.SessionCapturePayload
import com.simprints.id.data.images.repository.ImageRepository
import timber.log.Timber
import com.simprints.id.data.images.model.Path as CorePath

class FaceImageManagerImpl(private val coreImageRepository: ImageRepository,
                           private val coreEventRepository: EventRepository) : FaceImageManager {

    override suspend fun save(imageBytes: ByteArray, captureEventId: String): SecuredImageRef? =
        determinePath(captureEventId)?.let { path ->
            Timber.d("Saving face image ${path.compose()}")
            val securedImageRef = coreImageRepository.storeImageSecurely(imageBytes, path)

            return if (securedImageRef != null) {
                SecuredImageRef(securedImageRef.relativePath.toDomain())
            } else {
                Timber.e("Saving image failed for captureId $captureEventId")
                null
            }
        }

    private suspend fun determinePath(captureEventId: String): CorePath? =
        try {
            val currentSession = coreEventRepository.getCurrentCaptureSessionEvent()

            val projectId = (currentSession.payload as SessionCapturePayload).projectId
            val sessionId = currentSession.id
            CorePath(arrayOf(
                PROJECTS_PATH, projectId, SESSIONS_PATH, sessionId, FACES_PATH, "$captureEventId.jpg"
            ))
        } catch (t: Throwable) {
            Timber.e(t)
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
