package com.simprints.infra.images

import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.images.model.SecuredImageRef

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
        modality: GeneralConfiguration.Modality,
        sampleId: String,
        fileExtension: String,
        sampleBytes: ByteArray,
        optionalMetadata: Map<String, String> = emptyMap(),
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
