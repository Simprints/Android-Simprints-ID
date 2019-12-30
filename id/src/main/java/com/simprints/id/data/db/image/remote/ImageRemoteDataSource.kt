package com.simprints.id.data.db.image.remote

import com.simprints.core.images.SecuredImageRef

interface ImageRemoteDataSource {

    /**
     * Uploads an image
     *
     * @return true if successful
     */
    suspend fun uploadImage(image: SecuredImageRef): UploadResult

}
