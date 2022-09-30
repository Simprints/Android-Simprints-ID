package com.simprints.infra.images.remote

import com.google.firebase.storage.FirebaseStorage
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.LoginManager
import kotlinx.coroutines.tasks.await
import java.io.FileInputStream
import javax.inject.Inject

internal class ImageRemoteDataSourceImpl @Inject constructor(
    private val imageUrlProvider: ConfigManager,
    private val loginManager: LoginManager
) : ImageRemoteDataSource {

    override suspend fun uploadImage(
        imageStream: FileInputStream,
        imageRef: SecuredImageRef
    ): UploadResult {

        val firebaseProjectName = loginManager.getLegacyAppFallback().options.projectId

        return if (firebaseProjectName != null) {
            val projectId = loginManager.getSignedInProjectIdOrEmpty()

            if (projectId.isEmpty())
                return UploadResult(imageRef, UploadResult.Status.FAILED)

            val bucketUrl = imageUrlProvider.getProject(projectId).imageBucket

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
