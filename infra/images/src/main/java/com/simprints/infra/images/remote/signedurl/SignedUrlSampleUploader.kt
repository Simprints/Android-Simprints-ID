package com.simprints.infra.images.remote.signedurl

import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.scope.EventScopeEndCause
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.images.local.ImageLocalDataSource
import com.simprints.infra.images.metadata.ImageMetadataStore
import com.simprints.infra.images.remote.SampleUploader
import com.simprints.infra.images.remote.signedurl.usecase.FetchUploadUrlsPerSampleUseCase
import com.simprints.infra.images.remote.signedurl.usecase.PrepareImageUploadDataUseCase
import com.simprints.infra.images.remote.signedurl.usecase.UploadSampleWithTrackingUseCase
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SYNC
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.isActive
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

internal class SignedUrlSampleUploader @Inject constructor(
    private val configRepository: ConfigRepository,
    private val localDataSource: ImageLocalDataSource,
    private val eventRepository: EventRepository,
    private val metadataStore: ImageMetadataStore,
    private val prepareImageUploadData: PrepareImageUploadDataUseCase,
    private val uploadSampleWithTracking: UploadSampleWithTrackingUseCase,
    private val fetchUploadUrlsPerSample: FetchUploadUrlsPerSampleUseCase,
) : SampleUploader {
    override suspend fun uploadAllSamples(projectId: String): Boolean {
        var allImagesUploaded = true
        val batchSize = getBatchSize()
        val urlRequestScope = eventRepository.createEventScope(type = EventScopeType.SAMPLE_UP_SYNC)

        val sampleReferenceBatches = localDataSource
            .listImages(projectId)
            // Preparing the file for upload requires reading each of them to calculate md5 and size,
            // therefore splitting the list into batches before preparing allows to avoid some work in
            // cases where there are large amounts of files and the coroutine is being interrupted,
            // even if the result is that some requested batches are not at max size.
            .chunked(batchSize)
        for (batch in sampleReferenceBatches) {
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
            val sampleIdToUrlMap = fetchUploadUrlsPerSample(projectId, batchUploadData)

            for (sample in batchUploadData) {
                if (!coroutineContext.isActive) {
                    // Do not upload next image if coroutine is being cancelled
                    allImagesUploaded = false
                    break
                }

                val url = sampleIdToUrlMap[sample.sampleId]
                if (url == null) {
                    allImagesUploaded = false
                    Simber.i("Failed to fetch sample url", tag = SYNC)
                    continue
                }

                // Upload the sample to the fetched URL and clean up the local storage if successful
                val success = uploadSampleWithTracking(urlRequestScope, url, sample)
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

    private suspend fun getBatchSize(): Int = configRepository
        .getProjectConfiguration()
        .synchronization
        .samples.signedUrlBatchSize
}
