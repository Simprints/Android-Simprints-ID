package com.simprints.core.images.remote

import com.google.firebase.storage.FirebaseStorage
import com.simprints.core.images.model.SecuredImageRef
import kotlinx.coroutines.tasks.await
import java.io.FileInputStream

internal class ImageRemoteDataSourceImpl : ImageRemoteDataSource {

    override suspend fun uploadImage(
        imageStream: FileInputStream,
        imageRef: SecuredImageRef
    ): UploadResult {
        val rootRef = FirebaseStorage.getInstance().reference

        var fileRef = rootRef
        imageRef.path.parts.forEach { pathPart ->
            fileRef = fileRef.child(pathPart)
        }

        val uploadTask = fileRef.putStream(imageStream).await()

        val status = if (uploadTask.task.isSuccessful) {
            UploadResult.Status.SUCCESSFUL
        } else {
            UploadResult.Status.FAILED
        }

        return UploadResult(imageRef, status)
    }

}
