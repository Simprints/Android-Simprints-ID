package com.simprints.fingerprint.infra.imagedistortionconfig.remote

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

internal class ImageDistortionConfigRemoteRepo @Inject constructor(
    private val configManager: ConfigManager,
    private val authStore: AuthStore,
) {
    /**
     * Uploads the config file to the firebase storage at location projects/projectId/un20modules/serialNumber/calibration.dat
     * @param un20SerialNumber the serial number of the un20 module
     * @param configFile the config file to upload
     */
    suspend fun uploadConfig(
        un20SerialNumber: String,
        configFile: ByteArray,
    ): Boolean {
        log("Starting to upload image distortion config file")
        val firebaseProjectName = authStore.getCoreApp().options.projectId
        if (firebaseProjectName == null) {
            log("Firebase project name is null")
            return false
        }
        val projectId = authStore.signedInProjectId

        if (projectId.isEmpty()) {
            log("AuthStore projectId is empty")
            return false
        }

        val bucketUrl = configManager.getProject(projectId).imageBucket

        val rootRef = FirebaseStorage
            .getInstance(
                authStore.getCoreApp(),
                bucketUrl,
            ).reference
        val fileRef = rootRef.child("$PROJECTS_FOLDER/$projectId/$UN20_MODULES_FOLDER/$un20SerialNumber/$FILE_NAME")
        log("Checking if file exists")
        // try to get the metadata of the file if an error occurs, it means the file does not exist
        try {
            fileRef.metadata.await()
            log("Config file already exists")
            return true
        } catch (e: StorageException) {
            if (e.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) {
                throw e
            }
            log("Config file does not exist")
        }
        log("Uploading ${fileRef.path}")
        val uploadTask = fileRef.putBytes(configFile).await()
        return uploadTask.task.isSuccessful
    }

    private fun log(message: String) {
        Simber.i(message, tag = TAG)
    }

    companion object {
        private const val PROJECTS_FOLDER = "projects"
        private const val UN20_MODULES_FOLDER = "un20modules"
        private const val FILE_NAME = "calibration.dat"

        private const val TAG = "DISTORTION_FILE_UPLOAD"
    }
}
