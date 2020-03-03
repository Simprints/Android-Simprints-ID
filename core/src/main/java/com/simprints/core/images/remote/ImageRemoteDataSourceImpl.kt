package com.simprints.core.images.remote

import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.simprints.core.images.model.SecuredImageRef
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.FileInputStream

internal class ImageRemoteDataSourceImpl : ImageRemoteDataSource {

    private companion object {
        val firebaseProjectName = FirebaseApp.getInstance()?.options?.projectId
        val bucketName = "gs://$firebaseProjectName-images-eu"
    }

    override suspend fun uploadImage(
        imageStream: FileInputStream,
        imageRef: SecuredImageRef
    ): UploadResult {

        return if (firebaseProjectName != null) {
            val rootRef = FirebaseStorage.getInstance(bucketName).reference

            var fileRef = rootRef
            imageRef.relativePath.parts.forEach { pathPart ->
                fileRef = fileRef.child(pathPart)
            }

            Timber.d("Uploading ${fileRef.path}")

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
