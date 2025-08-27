package com.simprints.face.capture.usecases

import com.simprints.core.domain.modality.Modality
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.images.model.SecuredImageRef
import javax.inject.Inject

internal class SaveFaceSampleUseCase @Inject constructor(
    private val coreImageRepository: ImageRepository,
    private val sessionEventRepository: SessionEventRepository,
) {
    suspend operator fun invoke(
        imageBytes: ByteArray,
        captureEventId: String,
    ): SecuredImageRef? {
        val sessionScope = try {
            sessionEventRepository.getCurrentSessionScope()
        } catch (_: Throwable) {
            return null
        }

        return coreImageRepository.storeSample(
            projectId = sessionScope.projectId,
            sessionId = sessionScope.id,
            modality = Modality.FACE,
            sampleId = captureEventId,
            fileExtension = "jpg",
            sampleBytes = imageBytes,
        )
    }
}
