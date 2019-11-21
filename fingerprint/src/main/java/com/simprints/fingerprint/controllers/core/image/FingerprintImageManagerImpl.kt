package com.simprints.fingerprint.controllers.core.image

class FingerprintImageManagerImpl : FingerprintImageManager {

    override suspend fun save(imageBytes: ByteArray): FingerprintImageRef {
        // TODO : To delegate to core image manager
        return FingerprintImageRef("image uri")
    }
}
