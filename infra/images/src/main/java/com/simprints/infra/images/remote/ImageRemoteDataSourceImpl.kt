package com.simprints.infra.images.remote

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.tasks.await
import java.io.FileInputStream
import javax.inject.Inject

internal class ImageRemoteDataSourceImpl @Inject constructor(
    private val configManager: ConfigManager,
    private val authStore: AuthStore,
) : ImageRemoteDataSource {
    override suspend fun uploadImage(
        imageStream: FileInputStream,
        imageRef: SecuredImageRef,
        metadata: Map<String, String>,
    ): UploadResult {
        val firebaseProjectName = authStore.getLegacyAppFallback().options.projectId

        return if (firebaseProjectName != null) {
            val projectId = authStore.signedInProjectId

            if (projectId.isEmpty()) {
                Simber.i("AuthStore projectId is empty")
                return UploadResult(imageRef, UploadResult.Status.FAILED)
            }

            val bucketUrl = configManager.getProject(projectId).imageBucket

            val rootRef = FirebaseStorage
                .getInstance(
                    authStore.getLegacyAppFallback(),
                    bucketUrl,
                ).reference

            var fileRef = rootRef
            imageRef.relativePath.parts.forEach { pathPart ->
                fileRef = fileRef.child(pathPart)
            }

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

            val status = if (uploadTask.task.isSuccessful) {
                UploadResult.Status.SUCCESSFUL
            } else {
                UploadResult.Status.FAILED
            }

            UploadResult(imageRef, status)
        } else {
            Simber.i("Firebase projectId is null")
            UploadResult(imageRef, UploadResult.Status.FAILED)
        }
    }
}
