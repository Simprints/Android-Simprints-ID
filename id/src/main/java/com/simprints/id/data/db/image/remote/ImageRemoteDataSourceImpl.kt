package com.simprints.id.data.db.image.remote

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.simprints.core.images.SecuredImageRef
import java.io.File

class ImageRemoteDataSourceImpl : ImageRemoteDataSource {

    override fun uploadImage(image: SecuredImageRef, callback: ImageRemoteDataSource.Callback) {
        val rootRef = FirebaseStorage.getInstance().reference
        val file = File(image.path)
        val uri = Uri.fromFile(file)

        val fileRef = rootRef.child(file.name)
        fileRef.putFile(uri).run {
            addOnSuccessListener {
                callback.onImageUploaded(image)
            }

            addOnFailureListener {
                Log.e("ALAN_TEST", it.message, it)
            }
        }
    }

}
