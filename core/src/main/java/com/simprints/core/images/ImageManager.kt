package com.simprints.core.images

//TODO: to be developed
interface ImageManager {

    //Encryption involved
    suspend fun saveSecurely(image: ByteArray): SecuredImageRef
}

