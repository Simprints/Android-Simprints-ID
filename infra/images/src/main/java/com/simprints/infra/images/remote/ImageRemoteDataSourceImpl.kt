package com.simprints.infra.images.remote

import com.google.firebase.storage.FirebaseStorage
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.logging.Simber
import com.simprints.infra.authstore.AuthStore
import kotlinx.coroutines.tasks.await
import java.io.FileInputStream
import javax.inject.Inject

internal class ImageRemoteDataSourceImpl @Inject constructor(
    private val imageUrlProvider: ConfigManager,
    private val authStore: AuthStore
) : ImageRemoteDataSource {

    override suspend fun uploadImage(
        imageStream: FileInputStream,
        imageRef: SecuredImageRef
    ): UploadResult {

        val firebaseProjectName = authStore.getLegacyAppFallback().options.projectId

        return if (firebaseProjectName != null) {
            val projectId = authStore.signedInProjectId

            if (projectId.isEmpty())
                return UploadResult(imageRef, UploadResult.Status.FAILED)

            val bucketUrl = imageUrlProvider.getProject(projectId).imageBucket

            val rootRef = FirebaseStorage.getInstance(
                authStore.getLegacyAppFallback(),
                bucketUrl
            ).reference

            var fileRef = rootRef
            imageRef.relativePath.parts.forEach { pathPart ->
                fileRef = fileRef.child(pathPart)
            }

            Simber.d("Uploading ${fileRef.path}")

            val uploadTask = fileRef.putStream(imageStream).await()

            val status = if (uploadTask.task.isSuccessful) {
                UploadResult.Status.SUCCESSFUL
            } else {
                UploadResult.Status.FAILED
            }

            UploadResult(imageRef, status)
        } else {
            UploadResult(imageRef, UploadResult.Status.FAILED)
        }
    }


}
