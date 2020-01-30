package com.simprints.id.data.db.image.repository

import com.simprints.core.images.Path
import com.simprints.core.images.SecuredImageRef

/**
 * Repository for handling local and remote image file operations
 */
interface ImageRepository {

    /**
     * Encrypts and stores an image file locally
     *
     * @param imageBytes
     *        the image, in bytes
     * @param path
     *        the path of the image within the images root folder, including file name
     * @return a reference to the newly stored image, if successful, otherwise null
     * @see [com.simprints.id.data.db.image.local.ImageLocalDataSource.encryptAndStoreImage]
     */
    fun storeImageSecurely(
        imageBytes: ByteArray,
        path: Path
    ): SecuredImageRef?

    /**
     * Uploads all images stored locally and deletes if the upload has been successful
     *
     * @return true if all images have been successfully uploaded and deleted from the device
     */
    suspend fun uploadStoredImagesAndDelete(): Boolean

}
