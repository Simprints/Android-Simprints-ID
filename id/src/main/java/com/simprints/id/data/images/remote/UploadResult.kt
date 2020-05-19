package com.simprints.id.data.images.remote

import com.simprints.id.data.images.model.SecuredImageRef

/**
 * An upload result
 *
 * @property image
 *           the image
 * @property status
 *           the outcome of the operation, either successful or failed
 */
data class UploadResult(val image: SecuredImageRef, val status: Status) {

    fun isSuccessful() = status == Status.SUCCESSFUL

    enum class Status {
        SUCCESSFUL,
        FAILED
    }

}
