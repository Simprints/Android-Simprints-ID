package com.simprints.id.data.images.remote

import com.simprints.id.data.images.model.SecuredImageRef
import java.io.FileInputStream

/**
 * Interface for remote image file operations
 */
internal interface ImageRemoteDataSource {

    /**
     * Uploads an image
     *
     * @param imageStream the image file as a stream
     * @param imageRef a reference to the image to be uploaded
     *
     * @return the result of the operation.
     * @see [UploadResult]
     */
    suspend fun uploadImage(
        imageStream: FileInputStream,
        imageRef: SecuredImageRef
    ): UploadResult

}
