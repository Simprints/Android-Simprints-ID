package com.simprints.infra.images

import com.simprints.core.domain.image.SecuredImageRef
import com.simprints.core.domain.modality.Modality

/**
 * Repository for handling local and remote image file operations
 */
interface ImageRepository {
    /**
     * Encrypts and stores an sample file locally.
     * Path of the sample file within the images root folder will
     * be created based on the session ID, modality and file extension parameters.
     *
     * @param sampleBytes the sample file to be stored in bytes
     * @param projectId the id of the project
     * @param metadata arbitrary key-value pairs to be associated with the image
     *
     * @return a reference to the newly stored image, if successful, otherwise null
     */
    suspend fun storeSample(
        projectId: String,
        sessionId: String,
        modality: Modality,
        sampleId: String,
        fileExtension: String,
        sampleBytes: ByteArray,
        optionalMetadata: Map<String, String> = emptyMap(),
    ): SecuredImageRef?

    /**
     * Uploads all images stored locally for the project and deletes if the upload has been successful
     *
     * @param progressCallback optional callback to report current and max item counts of progress
     * @return true if all images have been successfully uploaded and deleted from the device
     */
    suspend fun uploadStoredImagesAndDelete(
        projectId: String,
        progressCallback: (suspend (Int, Int) -> Unit)? = null,
    ): Boolean

    /**
     * Deletes all images stored on the device
     */
    suspend fun deleteStoredImages()

    suspend fun getNumberOfImagesToUpload(projectId: String): Int
}
