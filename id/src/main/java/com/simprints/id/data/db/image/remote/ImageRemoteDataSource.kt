package com.simprints.id.data.db.image.remote

import com.simprints.core.images.SecuredImageRef

interface ImageRemoteDataSource {

    suspend fun uploadImage(image: SecuredImageRef): UploadResult

}
