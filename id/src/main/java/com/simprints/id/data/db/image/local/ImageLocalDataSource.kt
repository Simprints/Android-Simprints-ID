package com.simprints.id.data.db.image.local

import com.simprints.core.images.SecuredImageRef
import java.io.FileInputStream

interface ImageLocalDataSource {

    fun storeImage(imageBytes: ByteArray, filename: String): SecuredImageRef?
    fun readImage(path: SecuredImageRef): FileInputStream?
    fun listImages(): List<SecuredImageRef>
    fun deleteImage(path: SecuredImageRef): Boolean

}

