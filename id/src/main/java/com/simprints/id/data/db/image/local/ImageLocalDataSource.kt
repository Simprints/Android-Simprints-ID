package com.simprints.id.data.db.image.local

import com.simprints.core.images.Path
import com.simprints.core.images.SecuredImageRef
import java.io.FileInputStream

/**
 * Interface for local image file operations
 */
interface ImageLocalDataSource {

    /**
     * Encrypts and stores an image
     *
     * @param imageBytes
     *        the image, as a byte array
     * @param subDirs
     *        the sub-directories where the image will be stored, within the root images folder.
     *        e.g.: for [root images folder]/fingerprints/bad_scans [subDirs] should be
     *        @sample [Path(arrayOf("fingerprints", "bad_scans"))]
     * @see [Path]
     * @param fileName
     *        the file name
     * @return
     *        a reference to the path of the newly stored image, if successful, otherwise null
     */
    fun encryptAndStoreImage(
        imageBytes: ByteArray,
        subDirs: Path,
        fileName: String
    ): SecuredImageRef?

    /**
     * Decrypts an image
     *
     * @param image
     *        a reference to the path of the encrypted image
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
     * @param image
     *        the image to be deleted
     * @return true if the operation was successful
     */
    fun deleteImage(image: SecuredImageRef): Boolean

}

