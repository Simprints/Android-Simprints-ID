package com.simprints.id.data.db.image.repository

import com.simprints.core.images.SecuredImageRef
import com.simprints.id.data.db.image.remote.UploadResult

interface ImageRepository {

    suspend fun deleteImage(image: SecuredImageRef): Boolean
    suspend fun uploadImages(): List<UploadResult>

}
