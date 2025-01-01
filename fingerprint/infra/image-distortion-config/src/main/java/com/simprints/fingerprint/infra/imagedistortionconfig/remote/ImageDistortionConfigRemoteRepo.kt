package com.simprints.fingerprint.infra.imagedistortionconfig.remote

import com.google.firebase.storage.FirebaseStorage
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
     * @param serialNumber the serial number of the scanner
     * @param configFile the config file to upload
     */
    suspend fun uploadConfig(
        serialNumber: String,
        configFile: ByteArray,
    ): Boolean {
        val firebaseProjectName = authStore.getCoreApp().options.projectId
        if (firebaseProjectName == null) {
            Simber.i("Firebase project name is null")
            return false
        }
        val projectId = authStore.signedInProjectId

        if (projectId.isEmpty()) {
            Simber.i("AuthStore projectId is empty")
            return false
        }

        val bucketUrl = configManager.getProject(projectId).imageBucket

        val rootRef = FirebaseStorage
            .getInstance(
                authStore.getCoreApp(),
                bucketUrl,
            ).reference
        val folderRef = rootRef.child("$PROJECTS_FOLDER/$projectId/$UN20_MODULES_FOLDER/$serialNumber/")
        Simber.d("Uploading to ${folderRef.path}")
        // check if the folder is not empty then the file is already uploaded and we should not upload it again
        val listAll = folderRef.listAll().await()
        if (listAll.items.isNotEmpty()) {
            Simber.i("File already uploaded")
            return true
        }
        val fileRef = folderRef.child(FILE_NAME)
        Simber.d("Uploading ${fileRef.path}")
        val uploadTask = fileRef.putBytes(configFile).await()
        return uploadTask.task.isSuccessful
    }

    companion object {
        private const val PROJECTS_FOLDER = "projects"
        private const val UN20_MODULES_FOLDER = "un20modules"
        private const val FILE_NAME = "calibration.dat"
    }
}
