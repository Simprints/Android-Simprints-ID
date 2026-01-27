package com.simprints.infra.images.remote.signedurl.usecase

import com.simprints.infra.backendapi.BackendApiClient
import com.simprints.infra.images.remote.signedurl.SampleUploadData
import com.simprints.infra.images.remote.signedurl.api.ApiSampleUploadUrlRequest
import com.simprints.infra.images.remote.signedurl.api.SampleUploadApiInterface
import javax.inject.Inject

internal class FetchUploadUrlsPerSampleUseCase @Inject constructor(
    private val backendApiClient: BackendApiClient,
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
            backendApiClient.executeCall(SampleUploadApiInterface::class) { api ->
                api.getSampleUploadUrl(projectId, batch)
            }
        }.getOrMapFailure { emptyList() }
        .associate { it.sampleId to it.url }
}
