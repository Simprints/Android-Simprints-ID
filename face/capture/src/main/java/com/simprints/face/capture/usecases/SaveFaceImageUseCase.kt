package com.simprints.face.capture.usecases

import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.images.model.Path
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FACE_CAPTURE
import com.simprints.infra.logging.Simber
import javax.inject.Inject

internal class SaveFaceImageUseCase @Inject constructor(
    private val coreImageRepository: ImageRepository,
    private val sessionEventRepository: SessionEventRepository,
) {
    suspend operator fun invoke(
        imageBytes: ByteArray,
        captureEventId: String,
    ): SecuredImageRef? = determinePath(captureEventId)?.let { path ->
        Simber.d("Saving face image ${path.compose()}", tag = FACE_CAPTURE)
        val sessionScope = sessionEventRepository.getCurrentSessionScope()
        val projectId = sessionScope.projectId
        val securedImageRef =
            coreImageRepository.storeImageSecurely(imageBytes, projectId, path)

        if (securedImageRef != null) {
            SecuredImageRef(securedImageRef.relativePath)
        } else {
            Simber.i("Saving image failed for captureId $captureEventId", tag = FACE_CAPTURE)
            null
        }
    }

    private suspend fun determinePath(captureEventId: String): Path? = try {
        val sessionScope = sessionEventRepository.getCurrentSessionScope()
        val sessionId = sessionScope.id
        Path(
            arrayOf(
                SESSIONS_PATH,
                sessionId,
                FACES_PATH,
                "$captureEventId.jpg",
            ),
        )
    } catch (t: Throwable) {
        Simber.e("Error determining path for captureId=$captureEventId", t, tag = FACE_CAPTURE)
        null
    }

    companion object {
        const val SESSIONS_PATH = "sessions"
        const val FACES_PATH = "faces"
    }
}
