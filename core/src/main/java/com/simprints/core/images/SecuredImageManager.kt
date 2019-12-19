package com.simprints.core.images

import java.io.FileInputStream

interface SecuredImageManager {

    fun storeImageForEnrol(image: ByteArray, template: ByteArray): SecuredImageRef?
    fun storeImage(imageBytes: ByteArray, filename: String): SecuredImageRef?
    fun readImage(path: SecuredImageRef): FileInputStream?
    fun listImages(): List<SecuredImageRef>
    fun deleteImage(path: SecuredImageRef): Boolean

}

