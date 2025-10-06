package com.simprints.infra.images.remote.firebase

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.samples.SampleUpSyncRequestEvent
import com.simprints.infra.events.event.domain.models.scope.EventScopeEndCause
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.images.local.ImageLocalDataSource
import com.simprints.infra.images.metadata.ImageMetadataStore
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.images.remote.SampleUploader
import com.simprints.infra.images.usecase.SamplePathConverter
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SAMPLE_UPLOAD
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SYNC
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.tasks.await
import java.io.FileInputStream
import javax.inject.Inject

internal class FirebaseSampleUploader @Inject constructor(
    private val timeHelper: TimeHelper,
    private val configManager: ConfigManager,
    private val authStore: AuthStore,
    private val localDataSource: ImageLocalDataSource,
    private val metadataStore: ImageMetadataStore,
    private val samplePathUtil: SamplePathConverter,
    private val eventRepository: EventRepository,
) : SampleUploader {
    override suspend fun uploadAllSamples(
        projectId: String,
        progressCallback: (suspend (Int, Int) -> Unit)?,
    ): Boolean {
        val firebaseApp = authStore.getLegacyAppFallback()
        if (firebaseApp.options.projectId.isNullOrBlank()) {
            Simber.i("Firebase projectId is null", tag = SAMPLE_UPLOAD)
            return false
        }
        var allImagesUploaded = true

        Simber.i("Starting sample upload to Firebase storage", tag = SAMPLE_UPLOAD)
        val bucketUrl = configManager.getProject(projectId).imageBucket
        val rootRef = FirebaseStorage
            .getInstance(firebaseApp, bucketUrl)
            .reference

        val urlRequestScope = eventRepository.createEventScope(type = EventScopeType.SAMPLE_UP_SYNC)

        val sampleReferences = localDataSource.listImages(projectId)
        Simber.i("Images to upload ${sampleReferences.size}", tag = SAMPLE_UPLOAD)

        sampleReferences.forEachIndexed { index, imageRef ->
            Simber.i("Reading sample file: ${imageRef.relativePath.parts.last()}", tag = SAMPLE_UPLOAD)

            progressCallback?.invoke(index, sampleReferences.size)
            try {
                val requestStartTime = timeHelper.now()
                localDataSource.decryptImage(imageRef)?.let { stream ->
                    val metadata = metadataStore.getMetadata(imageRef.relativePath)

                    val task = uploadSample(rootRef, stream, imageRef, metadata)
                    if (task.task.isSuccessful) {
                        localDataSource.deleteImage(imageRef)
                        metadataStore.deleteMetadata(imageRef.relativePath)
                    } else {
                        allImagesUploaded = false
                        Simber.i("Failed to upload image without exception", tag = SAMPLE_UPLOAD)
                    }

                    eventRepository.addOrUpdateEvent(
                        scope = urlRequestScope,
                        event = SampleUpSyncRequestEvent(
                            createdAt = requestStartTime,
                            endedAt = timeHelper.now(),
                            requestId = null,
                            sampleId = samplePathUtil.extract(imageRef.relativePath)?.sampleId.orEmpty(),
                            size = task.bytesTransferred,
                            errorType = task.error?.javaClass?.simpleName,
                        ),
                    )
                }
            } catch (t: Throwable) {
                allImagesUploaded = false
                Simber.e("Failed to upload images", t, tag = SAMPLE_UPLOAD)
            }
        }
        eventRepository.closeEventScope(urlRequestScope, EventScopeEndCause.WORKFLOW_ENDED)

        return allImagesUploaded
    }

    private suspend fun uploadSample(
        rootRef: StorageReference,
        imageStream: FileInputStream,
        imageRef: SecuredImageRef,
        metadata: Map<String, String>,
    ): UploadTask.TaskSnapshot {
        val fileRef = imageRef.relativePath.parts
            .fold(rootRef) { ref, pathPart -> ref.child(pathPart) }

        Simber.i("Uploading ${fileRef.path.last()}", tag = SAMPLE_UPLOAD)

        return if (metadata.isEmpty()) {
            fileRef.putStream(imageStream).await()
        } else {
            val storeMetadata = StorageMetadata
                .Builder()
                .also { metadata.forEach { (key, value) -> it.setCustomMetadata(key, value) } }
                .build()
            fileRef.putStream(imageStream, storeMetadata).await()
        }
    }
}
