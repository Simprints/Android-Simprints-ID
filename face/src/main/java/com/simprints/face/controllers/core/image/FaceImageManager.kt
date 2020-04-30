package com.simprints.face.controllers.core.image

import com.simprints.face.data.moduleapi.face.responses.entities.SecuredImageRef

interface FaceImageManager {
    suspend fun save(imageBytes: ByteArray, captureEventId: String): SecuredImageRef?
}
