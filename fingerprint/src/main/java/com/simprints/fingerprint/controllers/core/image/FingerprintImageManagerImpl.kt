package com.simprints.fingerprint.controllers.core.image

import com.simprints.eventsystem.event.EventRepository
import com.simprints.fingerprint.data.domain.images.FingerprintImageRef
import com.simprints.fingerprint.data.domain.images.Path
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.logging.Simber
import javax.inject.Inject
import com.simprints.infra.images.model.Path as CorePath

class FingerprintImageManagerImpl @Inject constructor(
    private val coreImageRepository: ImageRepository,
    private val coreEventRepository: EventRepository,
) : FingerprintImageManager {

    override suspend fun save(
        imageBytes: ByteArray,
        captureEventId: String,
        fileExtension: String
    ): FingerprintImageRef? = determinePath(captureEventId, fileExtension)?.let { path ->
        Simber.d("Saving fingerprint image ${path.compose()}")
        val currentSession = coreEventRepository.getCurrentCaptureSessionEvent()
        val projectId = currentSession.payload.projectId

        val securedImageRef = coreImageRepository.storeImageSecurely(imageBytes, projectId, path)

        if (securedImageRef != null) {
            FingerprintImageRef(securedImageRef.relativePath.toDomain())
        } else {
            Simber.e("Saving image failed for captureId $captureEventId")
            null
        }
    }

    private suspend fun determinePath(captureEventId: String, fileExtension: String): CorePath? =
        try {
            val currentSession = coreEventRepository.getCurrentCaptureSessionEvent()
            val sessionId = currentSession.id
            CorePath(
                arrayOf(
                    SESSIONS_PATH,
                    sessionId,
                    FINGERPRINTS_PATH,
                    "$captureEventId.$fileExtension"
                )
            )
        } catch (t: Throwable) {
            Simber.e(t)
            null
        }

    private fun CorePath.toDomain(): Path = Path(this.parts)

    companion object {
        const val SESSIONS_PATH = "sessions"
        const val FINGERPRINTS_PATH = "fingerprints"
    }
}
