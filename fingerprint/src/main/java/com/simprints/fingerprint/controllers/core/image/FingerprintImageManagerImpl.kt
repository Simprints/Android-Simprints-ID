package com.simprints.fingerprint.controllers.core.image

import com.simprints.core.images.repository.ImageRepository
import com.simprints.core.tools.extentions.resumeSafely
import com.simprints.fingerprint.data.domain.images.FingerprintImageRef
import com.simprints.fingerprint.data.domain.images.Path
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import com.simprints.core.images.model.Path as CorePath

class FingerprintImageManagerImpl(private val coreImageRepository: ImageRepository,
                                  private val coreSessionEventsManager: SessionEventsManager) : FingerprintImageManager {

    override suspend fun save(imageBytes: ByteArray, captureEventId: String, fileExtension: String): FingerprintImageRef? =
        determinePath(captureEventId, fileExtension)?.let { path ->
            Timber.d("Saving fingerprint image ${path.compose()}")
            val securedImageRef = coreImageRepository.storeImageSecurely(imageBytes, path)

            return if (securedImageRef != null) {
                FingerprintImageRef(securedImageRef.relativePath.toDomain())
            } else {
                Timber.e("Saving image failed for captureId $captureEventId")
                null
            }
        }

    private suspend fun determinePath(captureEventId: String, fileExtension: String): CorePath? =
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine<CorePath?> { cont ->
                coreSessionEventsManager.getCurrentSession().subscribeBy(
                    onSuccess = { currentSession ->
                        val projectId = currentSession.projectId
                        val sessionId = currentSession.id
                        val path = CorePath(arrayOf(
                            PROJECTS_PATH, projectId, SESSIONS_PATH, sessionId, FINGERPRINTS_PATH, "$captureEventId.$fileExtension"
                        ))

                        cont.resumeSafely(path)
                    }, onError = { cont.resumeSafely(null) })
            }
        }

    private fun CorePath.toDomain(): Path =
        Path(this.parts)

    companion object {
        const val PROJECTS_PATH = "projects"
        const val SESSIONS_PATH = "sessions"
        const val FINGERPRINTS_PATH = "fingerprints"
    }
}
