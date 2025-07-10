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
    override suspend fun uploadAllSamples(projectId: String): Boolean {
        val firebaseApp = authStore.getLegacyAppFallback()
        val firebaseProjectName = firebaseApp.options.projectId

        if (!firebaseProjectName.isNullOrBlank()) {
            var allImagesUploaded = true

            val bucketUrl = configManager.getProject(projectId).imageBucket
            val rootRef = FirebaseStorage
                .getInstance(firebaseApp, bucketUrl)
                .reference

            localDataSource.listImages(projectId).forEach { imageRef ->
                try {
                    localDataSource.decryptImage(imageRef)?.let { stream ->
                        val metadata = metadataStore.getMetadata(imageRef.relativePath)
                        val uploadSuccessful = uploadSample(rootRef, stream, imageRef, metadata)
                        if (uploadSuccessful) {
                            localDataSource.deleteImage(imageRef)
                            metadataStore.deleteMetadata(imageRef.relativePath)
                        } else {
                            allImagesUploaded = false
                            Simber.i("Failed to upload image without exception", tag = SYNC)
                        }
                    }
                } catch (t: Throwable) {
                    allImagesUploaded = false
                    Simber.e("Failed to upload images", t, tag = SYNC)
                }
            }

            return allImagesUploaded
        } else {
            Simber.i("Firebase projectId is null")
            return false
        }
    }

    private suspend fun uploadSample(
        rootRef: StorageReference,
        imageStream: FileInputStream,
        imageRef: SecuredImageRef,
        metadata: Map<String, String>,
    ): Boolean {
        val fileRef = imageRef.relativePath.parts
            .fold(rootRef) { ref, pathPart -> ref.child(pathPart) }

        Simber.d("Uploading ${fileRef.path}")

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
