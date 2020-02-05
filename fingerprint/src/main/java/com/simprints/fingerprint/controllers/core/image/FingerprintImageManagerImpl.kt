package com.simprints.fingerprint.controllers.core.image

import com.simprints.core.images.repository.ImageRepository
import com.simprints.fingerprint.data.domain.images.FingerprintImageRef
import com.simprints.fingerprint.data.domain.images.Path
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import com.simprints.core.images.model.Path as CorePath

class FingerprintImageManagerImpl(private val coreImageRepository: ImageRepository,
                                  private val coreSessionEventsManager: SessionEventsManager) : FingerprintImageManager {

    override suspend fun save(imageBytes: ByteArray, captureEventId: String, fileExtension: String): FingerprintImageRef? =
        withContext(Dispatchers.IO) {

            val path = determinePath(captureEventId, fileExtension)
            Timber.d("Saving fingerprint image ${path.compose()}")
            val securedImageRef = coreImageRepository.storeImageSecurely(imageBytes, path)

            if (securedImageRef != null) {
                FingerprintImageRef(securedImageRef.path.toDomain())
            } else {
                Timber.e("Saving image failed for captureId $captureEventId")
                null
            }
        }

    private fun determinePath(captureEventId: String, fileExtension: String): CorePath {
        val currentSession = coreSessionEventsManager.getCurrentSession().blockingGet() // STOPSHIP : blockingGet()
        val projectId = currentSession.projectId
        val sessionId = currentSession.id

        return CorePath(arrayOf(
            PROJECTS_PATH, projectId, SESSIONS_PATH, sessionId, FINGERPRINTS_PATH, "$captureEventId.$fileExtension"
        ))
    }

    private fun CorePath.toDomain(): Path =
        Path(this.parts)

    companion object {
        const val PROJECTS_PATH = "projects"
        const val SESSIONS_PATH = "sessions"
        const val FINGERPRINTS_PATH = "fingerprints"
    }
}
