package com.simprints.id.data.images.remote

import com.google.firebase.storage.FirebaseStorage
import com.simprints.id.data.images.model.SecuredImageRef
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.LoginManager
import kotlinx.coroutines.tasks.await
import java.io.FileInputStream

internal class ImageRemoteDataSourceImpl(
    private val configManager: ConfigManager,
    private val loginManager: LoginManager
) : ImageRemoteDataSource {

    override suspend fun uploadImage(
        imageStream: FileInputStream,
        imageRef: SecuredImageRef
    ): UploadResult {

        val firebaseProjectName = loginManager.getLegacyAppFallback().options.projectId

        return if (firebaseProjectName != null) {
            val bucketUrl = configManager.getProject(loginManager.signedInProjectId).imageBucket

            val rootRef = FirebaseStorage.getInstance(
                loginManager.getLegacyAppFallback(),
                bucketUrl
            ).reference

            var fileRef = rootRef
            imageRef.relativePath.parts.forEach { pathPart ->
                fileRef = fileRef.child(pathPart)
            }

            Simber.d("Uploading ${fileRef.path}")

            val uploadTask = fileRef.putStream(imageStream).await()

            val status = if (uploadTask.task.isSuccessful) {
                UploadResult.Status.SUCCESSFUL
            } else {
                UploadResult.Status.FAILED
            }

            UploadResult(imageRef, status)
        } else {
            UploadResult(imageRef, UploadResult.Status.FAILED)
        }
    }


}
