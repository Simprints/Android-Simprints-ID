package com.simprints.infra.images.remote.signedurl

import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.samples.SampleUpSyncRequestEvent
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.scope.EventScopeEndCause
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.images.local.ImageLocalDataSource
import com.simprints.infra.images.metadata.ImageMetadataStore
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.images.remote.SampleUploader
import com.simprints.infra.images.remote.signedurl.api.ApiSampleUploadUrlRequest
import com.simprints.infra.images.remote.signedurl.api.SampleUploadApiInterface
import com.simprints.infra.images.remote.signedurl.api.SampleUploadRequestBody
import com.simprints.infra.images.usecase.CalculateFileMd5AndSizeUseCase
import com.simprints.infra.images.usecase.SamplePathConvertor
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SYNC
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.SimNetwork
import kotlinx.coroutines.isActive
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

internal class SignedUrlSampleUploader @Inject constructor(
    private val timeHelper: TimeHelper,
    private val authStore: AuthStore,
    private val eventRepository: EventRepository,
    private val configRepository: ConfigRepository,
    private val localDataSource: ImageLocalDataSource,
    private val metadataStore: ImageMetadataStore,
    private val samplePathUtil: SamplePathConvertor,
    private val calculateFileMd5AndSize: CalculateFileMd5AndSizeUseCase,
) : SampleUploader {
    override suspend fun uploadAllSamples(projectId: String): Boolean {
        val client = getClient()
        val batchSize = getBatchSize()

        val urlRequestScope = eventRepository.createEventScope(type = EventScopeType.SAMPLE_UP_SYNC)
        var allImagesUploaded = true

        val sampleBatches = localDataSource.listImages(projectId).chunked(batchSize)
        for (batch in sampleBatches) {
            if (!coroutineContext.isActive) {
                // Do not process next batch if coroutine is being cancelled
                allImagesUploaded = false
                break
            }

            // Read batch of images to calculate hashes, size and other meta data
            val batchUploadData = batch.mapNotNull { imageRef ->
                try {
                    prepareImageUploadData(imageRef).also {
                        if (it == null) {
                            allImagesUploaded = false
                            Simber.i("Failed to read image file without exception", tag = SYNC)
                        }
                    }
                } catch (t: Throwable) {
                    allImagesUploaded = false
                    Simber.e("Failed to read image file", t, tag = SYNC)
                    null
                }
            }

            // Fetch upload urls for each image
            val sampleUrlMap = fetchUploadUrlsPerSample(batchUploadData, batchSize, client, projectId)

            for (sample in batchUploadData) {
                if (!coroutineContext.isActive) {
                    // Do not upload next image if coroutine is being cancelled
                    allImagesUploaded = false
                    break
                }

                val url = sampleUrlMap[sample.sampleId]
                if (url == null) {
                    allImagesUploaded = false
                    Simber.i("Failed to fetch sample url", tag = SYNC)
                    continue
                }

                // Upload the sample to the fetched URL and
                val success = uploadSampleWithEventTracking(client, sample, url, urlRequestScope)
                if (success) {
                    localDataSource.deleteImage(sample.imageRef)
                    metadataStore.deleteMetadata(sample.imageRef.relativePath)
                } else {
                    allImagesUploaded = false
                }
            }
        }
        eventRepository.closeEventScope(urlRequestScope, EventScopeEndCause.WORKFLOW_ENDED)

        return allImagesUploaded
    }

    private suspend fun getClient(): SimNetwork.SimApiClient<SampleUploadApiInterface> =
        authStore.buildClient(SampleUploadApiInterface::class)

    private suspend fun prepareImageUploadData(imageRef: SecuredImageRef): SampleUploadData? = localDataSource
        .decryptImage(imageRef)
        ?.use { stream -> calculateFileMd5AndSize(stream) }
        ?.let { (md5, size) ->
            val (sessionId, modality, sampleId) = samplePathUtil.extract(imageRef.relativePath) ?: return null
            val metadata = metadataStore.getMetadata(imageRef.relativePath)

            SampleUploadData(
                imageRef = imageRef,
                sampleId = sampleId,
                sessionId = sessionId,
                md5 = md5,
                size = size,
                modality = modality.name,
                metadata = metadata,
            )
        }

    private suspend fun fetchUploadUrlsPerSample(
        batchUploadData: List<SampleUploadData>,
        batchSize: Int,
        client: SimNetwork.SimApiClient<SampleUploadApiInterface>,
        projectId: String,
    ): Map<String, String> = batchUploadData
        .map {
            ApiSampleUploadUrlRequest(
                sampleId = it.sampleId,
                sessionId = it.sessionId,
                modality = it.modality,
                md5 = it.md5,
                metadata = it.metadata,
            )
        }.chunked(batchSize)
        .map { batch ->

            try {
                client.executeCall { api -> api.getSampleUploadUrl(projectId, batch) }
            } catch (_: Exception) {
                emptyList()
            }
        }.flatten()
        .associate { it.sampleId to it.url }

    private suspend fun getBatchSize(): Int = configRepository
        .getProjectConfiguration()
        .synchronization
        .samples.signedUrlBatchSize

    private suspend fun uploadSampleWithEventTracking(
        client: SimNetwork.SimApiClient<SampleUploadApiInterface>,
        sample: SampleUploadData,
        url: String,
        urlRequestScope: EventScope,
    ): Boolean {
        val requestId = UUID.randomUUID().toString()
        val requestStartTime = timeHelper.now()

        val errorType = uploadSample(client, sample, url, requestId)
        eventRepository.addOrUpdateEvent(
            scope = urlRequestScope,
            event = SampleUpSyncRequestEvent(
                createdAt = requestStartTime,
                endedAt = timeHelper.now(),
                requestId = requestId,
                sampleId = sample.sampleId,
                size = sample.size,
                errorType = errorType,
            ),
        )
        return errorType.isNullOrBlank()
    }

    private suspend fun uploadSample(
        client: SimNetwork.SimApiClient<SampleUploadApiInterface>,
        sampleData: SampleUploadData,
        url: String,
        requestId: String,
    ): String? = localDataSource.decryptImage(sampleData.imageRef)?.use { stream ->
        try {
            val response = client.executeCall { api ->
                api.uploadFile(
                    uploadUrl = url,
                    requestId = requestId,
                    md5 = sampleData.md5,
                    requestBody = SampleUploadRequestBody(stream, sampleData.size),
                )
            }
            if (response.isSuccessful) {
                null
            } else {
                response.errorBody()?.string().also {
                    Simber.i("Failed to upload image: $it", tag = SYNC)
                }
            }
        } catch (e: Exception) {
            Simber.e("Failed to upload image", e, tag = SYNC)
            e.javaClass.simpleName
        }
    }

    private data class SampleUploadData(
        val imageRef: SecuredImageRef,
        val sampleId: String,
        val sessionId: String,
        val modality: String,
        val md5: String,
        val size: Long,
        val metadata: Map<String, String>,
    )
}
