package com.simprints.id.data.db.image.remote

import com.simprints.core.images.SecuredImageRef

/**
 * Interface for remote image file operations
 */
interface ImageRemoteDataSource {

    /**
     * Uploads an image
     *
     * @param image the image to be uploaded
     * @return the result of the operation.
     * @see [UploadResult]
     */
    suspend fun uploadImage(image: SecuredImageRef): UploadResult

}
