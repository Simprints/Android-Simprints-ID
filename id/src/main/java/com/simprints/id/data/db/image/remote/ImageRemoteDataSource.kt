package com.simprints.id.data.db.image.remote

import com.simprints.core.images.SecuredImageRef
import java.io.FileInputStream

/**
 * Interface for remote image file operations
 */
interface ImageRemoteDataSource {

    /**
     * Uploads an image
     *
     * @param imageStream the image file as a stream
     * @param imageRef a reference to the image to be uploaded
     * @return the result of the operation.
     * @see [UploadResult]
     */
    suspend fun uploadImage(imageStream: FileInputStream, imageRef: SecuredImageRef): UploadResult

}
