package com.simprints.infra.images.remote.firestore

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.images.local.ImageLocalDataSource
import com.simprints.infra.images.metadata.ImageMetadataStore
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.images.remote.SampleUploader
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SAMPLE_UPLOAD
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SYNC
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.tasks.await
import java.io.FileInputStream
import javax.inject.Inject

internal class FirestoreSampleUploader @Inject constructor(
    private val configManager: ConfigManager,
    private val authStore: AuthStore,
    private val localDataSource: ImageLocalDataSource,
    private val metadataStore: ImageMetadataStore,
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

        Simber.i("Starting sample upload to Firestore")
        val bucketUrl = configManager.getProject(projectId).imageBucket
        val rootRef = FirebaseStorage
            .getInstance(firebaseApp, bucketUrl)
            .reference

        val sampleReferences = localDataSource.listImages(projectId)
        sampleReferences.forEachIndexed { index, imageRef ->
            Simber.i("Reading sample file: ${imageRef.relativePath.parts.last()}", tag = SAMPLE_UPLOAD)
            progressCallback?.invoke(index, sampleReferences.size)
            try {
                localDataSource.decryptImage(imageRef)?.let { stream ->
                    val metadata = metadataStore.getMetadata(imageRef.relativePath)
                    val uploadSuccessful = uploadSample(rootRef, stream, imageRef, metadata)
                    if (uploadSuccessful) {
                        localDataSource.deleteImage(imageRef)
                        metadataStore.deleteMetadata(imageRef.relativePath)
                    } else {
                        allImagesUploaded = false
                        Simber.i("Failed to upload image without exception", tag = SAMPLE_UPLOAD)
                    }
                }
            } catch (t: Throwable) {
                allImagesUploaded = false
                Simber.e("Failed to upload images", t, tag = SYNC)
            }
        }

        return allImagesUploaded
    }

    private suspend fun uploadSample(
        rootRef: StorageReference,
        imageStream: FileInputStream,
        imageRef: SecuredImageRef,
        metadata: Map<String, String>,
    ): Boolean {
        val fileRef = imageRef.relativePath.parts
            .fold(rootRef) { ref, pathPart -> ref.child(pathPart) }

        Simber.i("Uploading ${fileRef.path.last()}", tag = SAMPLE_UPLOAD)

        val uploadTask = if (metadata.isEmpty()) {
            fileRef.putStream(imageStream).await()
        } else {
            val storeMetadata = StorageMetadata
                .Builder()
                .also { metadata.forEach { (key, value) -> it.setCustomMetadata(key, value) } }
                .build()
            fileRef.putStream(imageStream, storeMetadata).await()
        }
        return uploadTask.task.isSuccessful
    }
}
