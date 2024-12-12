package com.simprints.infra.images.remote

import com.simprints.infra.images.model.SecuredImageRef
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
     * @param metadata arbitrary key-value pairs to be associated with the image
     *
     * @return the result of the operation.
     * @see [UploadResult]
     */
    suspend fun uploadImage(
        imageStream: FileInputStream,
        imageRef: SecuredImageRef,
        metadata: Map<String, String>,
    ): UploadResult
}
