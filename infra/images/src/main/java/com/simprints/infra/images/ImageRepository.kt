package com.simprints.infra.images

import com.simprints.infra.images.model.Path
import com.simprints.infra.images.model.SecuredImageRef

/**
 * Repository for handling local and remote image file operations
 */
interface ImageRepository {
    /**
     * Encrypts and stores an image file locally
     *
     * @param imageBytes the image, in bytes
     * @param projectId the id of the project
     * @param relativePath the path of the image within the images root folder, including file name
     * @param metadata arbitrary key-value pairs to be associated with the image
     *
     * @return a reference to the newly stored image, if successful, otherwise null
     * @see [com.simprints.infra.images.local.ImageLocalDataSource.encryptAndStoreImage]
     */
    suspend fun storeImageSecurely(
        imageBytes: ByteArray,
        projectId: String,
        relativePath: Path,
        metadata: Map<String, String> = emptyMap(),
    ): SecuredImageRef?

    /**
     * Uploads all images stored locally for the project and deletes if the upload has been successful
     *
     * @return true if all images have been successfully uploaded and deleted from the device
     */
    suspend fun uploadStoredImagesAndDelete(projectId: String): Boolean

    /**
     * Deletes all images stored on the device
     */
    suspend fun deleteStoredImages()

    suspend fun getNumberOfImagesToUpload(projectId: String): Int
}
