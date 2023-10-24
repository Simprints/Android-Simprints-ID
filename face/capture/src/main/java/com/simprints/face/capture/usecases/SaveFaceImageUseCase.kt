package com.simprints.face.capture.usecases

import com.simprints.face.capture.models.Path
import com.simprints.face.capture.models.SecuredImageRef
import com.simprints.infra.events.EventRepository
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.logging.Simber
import javax.inject.Inject
import com.simprints.infra.images.model.Path as CorePath

internal class SaveFaceImageUseCase @Inject constructor(
    private val coreImageRepository: ImageRepository,
    private val coreEventRepository: EventRepository,
) {

    suspend operator fun invoke(imageBytes: ByteArray, captureEventId: String): SecuredImageRef? =
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

    private suspend fun determinePath(captureEventId: String): CorePath? = try {
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

    private fun CorePath.toDomain(): Path = Path(this.parts)

    companion object {
        const val SESSIONS_PATH = "sessions"
        const val FACES_PATH = "faces"
    }
}
