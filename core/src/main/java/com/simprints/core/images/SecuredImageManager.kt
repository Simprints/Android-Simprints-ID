package com.simprints.core.images

interface SecuredImageManager {

    //Encryption involved
    suspend fun saveSecurely(image: ByteArray): SecuredImageRef

}

