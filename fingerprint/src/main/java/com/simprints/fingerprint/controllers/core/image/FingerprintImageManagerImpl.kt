package com.simprints.fingerprint.controllers.core.image

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FingerprintImageManagerImpl : FingerprintImageManager {

    override suspend fun save(imageBytes: ByteArray): FingerprintImageRef = withContext(Dispatchers.IO) {
        // TODO : To delegate to core image manager
        FingerprintImageRef("image uri")
    }
}
