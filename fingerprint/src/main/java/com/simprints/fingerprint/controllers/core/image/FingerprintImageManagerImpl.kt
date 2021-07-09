package com.simprints.fingerprint.controllers.core.image

import com.simprints.eventsystem.event.domain.models.session.SessionCaptureEvent.SessionCapturePayload
import com.simprints.fingerprint.data.domain.images.FingerprintImageRef
import com.simprints.fingerprint.data.domain.images.Path
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.logging.Simber
import com.simprints.id.data.images.model.Path as CorePath

class FingerprintImageManagerImpl(private val coreImageRepository: ImageRepository,
                                  private val coreEventRepository: com.simprints.eventsystem.event.EventRepository
) : FingerprintImageManager {

    override suspend fun save(imageBytes: ByteArray, captureEventId: String, fileExtension: String): FingerprintImageRef? =
        determinePath(captureEventId, fileExtension)?.let { path ->
            Simber.d("Saving fingerprint image ${path.compose()}")
            val securedImageRef = coreImageRepository.storeImageSecurely(imageBytes, path)

            return if (securedImageRef != null) {
                FingerprintImageRef(securedImageRef.relativePath.toDomain())
            } else {
                Simber.e("Saving image failed for captureId $captureEventId")
                null
            }
        }

    private suspend fun determinePath(captureEventId: String, fileExtension: String): CorePath? =
        try {
            val currentSession = coreEventRepository.getCurrentCaptureSessionEvent()

            val projectId = (currentSession.payload as SessionCapturePayload).projectId
            val sessionId = currentSession.id
            CorePath(arrayOf(
                PROJECTS_PATH, projectId, SESSIONS_PATH, sessionId, FINGERPRINTS_PATH, "$captureEventId.$fileExtension"
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
        const val FINGERPRINTS_PATH = "fingerprints"
    }
}
