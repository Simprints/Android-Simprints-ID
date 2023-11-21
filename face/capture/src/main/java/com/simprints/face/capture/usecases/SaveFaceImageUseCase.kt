package com.simprints.face.capture.usecases

import com.simprints.infra.events.EventRepository
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.logging.Simber
import javax.inject.Inject
import com.simprints.infra.images.model.Path

internal class SaveFaceImageUseCase @Inject constructor(
    private val coreImageRepository: ImageRepository,
    private val coreEventRepository: EventRepository,
) {

    suspend operator fun invoke(imageBytes: ByteArray, captureEventId: String): SecuredImageRef? =
        determinePath(captureEventId)?.let { path ->
            Simber.d("Saving face image ${path.compose()}")
            val currentSession = coreEventRepository.getCurrentCaptureSessionEvent()
            val projectId = currentSession.payload.projectId
            val securedImageRef =
                coreImageRepository.storeImageSecurely(imageBytes, projectId, path)

            if (securedImageRef != null) {
                SecuredImageRef(securedImageRef.relativePath)
            } else {
                Simber.e("Saving image failed for captureId $captureEventId")
                null
            }
        }

    private suspend fun determinePath(captureEventId: String): Path? = try {
        val currentSession = coreEventRepository.getCurrentCaptureSessionEvent()
        val sessionId = currentSession.id
        Path(
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

    companion object {

        const val SESSIONS_PATH = "sessions"
        const val FACES_PATH = "faces"
    }
}
