package com.simprints.infra.images.remote.signedurl.usecase

import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.images.remote.signedurl.SampleUploadData
import com.simprints.infra.images.remote.signedurl.api.ApiSampleUploadUrlRequest
import com.simprints.infra.images.remote.signedurl.api.SampleUploadApiInterface
import javax.inject.Inject

internal class FetchUploadUrlsPerSampleUseCase @Inject constructor(
    private val authStore: AuthStore,
) {
    suspend operator fun invoke(
        projectId: String,
        batchUploadData: List<SampleUploadData>,
    ): Map<String, String> = batchUploadData
        .map {
            ApiSampleUploadUrlRequest(
                sampleId = it.sampleId,
                sessionId = it.sessionId,
                modality = it.modality,
                md5 = it.md5,
                metadata = it.metadata,
            )
        }.let { batch ->
            try {
                authStore
                    .buildClient(SampleUploadApiInterface::class)
                    .executeCall { api -> api.getSampleUploadUrl(projectId, batch) }
            } catch (_: Exception) {
                emptyList()
            }
        }.associate { it.sampleId to it.url }
}
