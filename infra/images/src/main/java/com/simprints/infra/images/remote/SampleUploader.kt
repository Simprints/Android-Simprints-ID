package com.simprints.infra.images.remote

/**
 * Interface for remote image file operations
 */
internal interface SampleUploader {
    /**
     * Uploads all locally stored samples.
     * On successful upload, the file and the associated metadata are deleted.
     */
    suspend fun uploadAllSamples(projectId: String, progressCallback: (suspend (Int, Int) -> Unit)? = null): Boolean
}
