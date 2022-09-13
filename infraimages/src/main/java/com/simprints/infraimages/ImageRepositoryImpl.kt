package com.simprints.infraimages

import android.content.Context
import com.simprints.core.sharedinterfaces.ImageUrlProvider
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.LoginManager
import com.simprints.infraimages.local.ImageLocalDataSource
import com.simprints.infraimages.local.ImageLocalDataSourceImpl
import com.simprints.infraimages.model.Path
import com.simprints.infraimages.model.SecuredImageRef
import com.simprints.infraimages.remote.ImageRemoteDataSource
import com.simprints.infraimages.remote.ImageRemoteDataSourceImpl
import javax.inject.Inject

class ImageRepositoryImpl @Inject internal constructor(
    private val localDataSource: ImageLocalDataSource,
    private val remoteDataSource: ImageRemoteDataSource
) : ImageRepository {

    constructor(
        context: Context,
        imageUrlProvider: ImageUrlProvider,
        loginManager: LoginManager
    ) : this(
        ImageLocalDataSourceImpl(context),
        ImageRemoteDataSourceImpl(imageUrlProvider, loginManager)
    )

    override fun storeImageSecurely(imageBytes: ByteArray, relativePath: Path): SecuredImageRef? {
        return localDataSource.encryptAndStoreImage(imageBytes, relativePath)
    }

    override fun getNumberOfImagesToUpload(): Int = localDataSource.listImages().count()

    override suspend fun uploadStoredImagesAndDelete(): Boolean {
        var allImagesUploaded = true

        val images = localDataSource.listImages()
        images.forEach { imageRef ->
            try {
                localDataSource.decryptImage(imageRef)?.let { stream ->
                    val uploadResult = remoteDataSource.uploadImage(stream, imageRef)
                    if (uploadResult.isUploadSuccessful()) {
                        localDataSource.deleteImage(imageRef)
                    } else {
                        allImagesUploaded = false
                    }
                }
            } catch (t: Throwable) {
                allImagesUploaded = false
                Simber.d(t)
            }
        }

        return allImagesUploaded
    }

    override fun deleteStoredImages() = with(localDataSource) {
        listImages().forEach {
            deleteImage(it)
        }
    }
}
