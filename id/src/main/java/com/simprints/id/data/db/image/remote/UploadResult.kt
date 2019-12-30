package com.simprints.id.data.db.image.remote

import com.simprints.core.images.SecuredImageRef

data class UploadResult(val image: SecuredImageRef, val status: Status) {

    enum class Status {
        SUCCESSFUL,
        FAILED
    }

}
