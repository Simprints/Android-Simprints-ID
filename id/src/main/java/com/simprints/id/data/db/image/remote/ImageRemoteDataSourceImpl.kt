package com.simprints.id.data.db.image.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.simprints.core.images.SecuredImageRef
import kotlinx.coroutines.tasks.await
import java.io.File

class ImageRemoteDataSourceImpl : ImageRemoteDataSource {

    override suspend fun uploadImage(image: SecuredImageRef): UploadResult {
        val rootRef = FirebaseStorage.getInstance().reference
        val file = File(image.path)
        val uri = Uri.fromFile(file)

        val fileRef = rootRef.child(file.name)
        val uploadTask = fileRef.putFile(uri).await()

        val status = if (uploadTask.bytesTransferred == file.length()) {
            UploadResult.Status.SUCCESSFUL
        } else {
            UploadResult.Status.FAILED
        }

        return UploadResult(image, status)
    }

}
