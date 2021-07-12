package com.simprints.id.data.images.remote

import com.google.firebase.storage.FirebaseStorage
import com.simprints.id.data.db.common.FirebaseManagerImpl.Companion.getLegacyAppFallback
import com.simprints.id.data.images.model.SecuredImageRef
import com.simprints.id.network.BaseUrlProvider
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.FileInputStream

internal class ImageRemoteDataSourceImpl(
    private val baseUrlProvider: BaseUrlProvider
) : ImageRemoteDataSource {

    override suspend fun uploadImage(
        imageStream: FileInputStream,
        imageRef: SecuredImageRef
    ): UploadResult {

        val firebaseProjectName = getLegacyAppFallback().options.projectId

        return if (firebaseProjectName != null) {
            val bucketUrl = baseUrlProvider.getImageStorageBucketUrl()
                ?: return UploadResult(imageRef, UploadResult.Status.FAILED)

            val rootRef = FirebaseStorage.getInstance(getLegacyAppFallback(), bucketUrl).reference

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
