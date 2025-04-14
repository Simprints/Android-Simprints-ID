package com.simprints.document.capture.usecases

import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.images.model.Path
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.DOCUMENT_CAPTURE
import com.simprints.infra.logging.Simber
import javax.inject.Inject

internal class SaveDocumentImageUseCase @Inject constructor(
    private val coreImageRepository: ImageRepository,
    private val sessionEventRepository: SessionEventRepository,
) {
    suspend operator fun invoke(
        imageBytes: ByteArray,
        captureEventId: String,
    ): SecuredImageRef? = determinePath(captureEventId)?.let { path ->
        Simber.d("Saving document image ${path.compose()}", tag = DOCUMENT_CAPTURE)
        val sessionScope = sessionEventRepository.getCurrentSessionScope()
        val projectId = sessionScope.projectId
        val securedImageRef =
            coreImageRepository.storeImageSecurely(imageBytes, projectId, path)

        if (securedImageRef != null) {
            SecuredImageRef(securedImageRef.relativePath)
        } else {
            Simber.i("Saving image failed for captureId $captureEventId", tag = DOCUMENT_CAPTURE)
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
                DOCUMENTS_PATH,
                "$captureEventId.jpg",
            ),
        )
    } catch (t: Throwable) {
        Simber.e("Error determining path for captureId=$captureEventId", t, tag = DOCUMENT_CAPTURE)
        null
    }

    companion object {
        const val SESSIONS_PATH = "sessions"
        const val DOCUMENTS_PATH = "documents"
    }
}
