package com.simprints.core.images.local

import com.simprints.core.images.model.Path
import com.simprints.core.images.model.SecuredImageRef
import java.io.FileInputStream

/**
 * Interface for local image file operations
 */
internal interface ImageLocalDataSource {

    /**
     * Encrypts and stores an image
     *
     * @param imageBytes
     *        the image, as a byte array
     * @param relativePath
     *        the path within the root images folder where the image will be stored,
     *        including file name.
     *        e.g.: for [root images folder]/fingerprints/bad_scans/image1.png [path] should be
     *        @sample [Path(arrayOf("fingerprints", "bad_scans", "image1.png"))]
     * @see [Path]
     * @return a reference to the newly stored image, if successful, otherwise null
     */
    fun encryptAndStoreImage(imageBytes: ByteArray, relativePath: Path): SecuredImageRef?

    /**
     * Decrypts an image
     *
     * @param image a reference to the path of the encrypted image
     * @return a stream for the decrypted image, if the operation was successful, otherwise null
     */
    fun decryptImage(image: SecuredImageRef): FileInputStream?

    /**
     * Recursively lists all images contained in the root images folder
     *
     * @return all image files found
     */
    fun listImages(): List<SecuredImageRef>

    /**
     * Deletes an image
     *
     * @param image the image to be deleted
     * @return true if the operation was successful
     */
    fun deleteImage(image: SecuredImageRef): Boolean

}

