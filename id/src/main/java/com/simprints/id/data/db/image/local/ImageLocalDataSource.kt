package com.simprints.id.data.db.image.local

import com.simprints.core.images.Path
import com.simprints.core.images.SecuredImageRef
import java.io.FileInputStream

interface ImageLocalDataSource {

    fun storeImage(imageBytes: ByteArray, subDirs: Path, fileName: String): SecuredImageRef?
    fun readImage(image: SecuredImageRef): FileInputStream?
    fun listImages(): List<SecuredImageRef>
    fun deleteImage(image: SecuredImageRef): Boolean

}

