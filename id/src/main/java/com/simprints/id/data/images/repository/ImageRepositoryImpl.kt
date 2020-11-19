package com.simprints.id.data.images.repository

import android.content.Context
import com.simprints.id.data.images.local.ImageLocalDataSource
import com.simprints.id.data.images.local.ImageLocalDataSourceImpl
import com.simprints.id.data.images.model.Path
import com.simprints.id.data.images.model.SecuredImageRef
import com.simprints.id.data.images.remote.ImageRemoteDataSource
import com.simprints.id.data.images.remote.ImageRemoteDataSourceImpl
import com.simprints.id.network.BaseUrlProvider
import timber.log.Timber

class ImageRepositoryImpl internal constructor(
    private val localDataSource: ImageLocalDataSource,
    private val remoteDataSource: ImageRemoteDataSource
) : ImageRepository {

    constructor(context: Context, baseUrlProvider: BaseUrlProvider) : this(
        ImageLocalDataSourceImpl(context),
        ImageRemoteDataSourceImpl(baseUrlProvider)
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
                Timber.d(t)
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
