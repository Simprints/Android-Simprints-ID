package com.simprints.id.data.db.image.remote

import com.google.firebase.storage.FirebaseStorage
import com.simprints.core.images.SecuredImageRef
import kotlinx.coroutines.tasks.await
import java.io.FileInputStream

class ImageRemoteDataSourceImpl : ImageRemoteDataSource {

    override suspend fun uploadImage(
        imageStream: FileInputStream,
        imageRef: SecuredImageRef
    ): UploadResult {
        val rootRef = FirebaseStorage.getInstance().reference.child("images")

        var fileRef = rootRef
        imageRef.relativePath.dirs.forEach { dir ->
            fileRef = rootRef.child(dir)
        }
        fileRef = fileRef.child(imageRef.getFileName())

        val uploadTask = fileRef.putStream(imageStream).await()

        val status = if (uploadTask.task.isSuccessful) {
            UploadResult.Status.SUCCESSFUL
        } else {
            UploadResult.Status.FAILED
        }

        return UploadResult(imageRef, status)
    }

}
