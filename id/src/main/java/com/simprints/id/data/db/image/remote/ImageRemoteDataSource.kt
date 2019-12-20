package com.simprints.id.data.db.image.remote

import com.simprints.core.images.SecuredImageRef

interface ImageRemoteDataSource {

    fun uploadImage(image: SecuredImageRef, callback: Callback)

    interface Callback {
        fun onImageUploaded(image: SecuredImageRef)
    }

}
