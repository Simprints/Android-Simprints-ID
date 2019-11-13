package com.simprints.core.images

//TODO: to be developed
interface SecuredImageManager {

    //Encryption involved
    suspend fun saveSecurely(image: ByteArray): SecuredImageRef
}

