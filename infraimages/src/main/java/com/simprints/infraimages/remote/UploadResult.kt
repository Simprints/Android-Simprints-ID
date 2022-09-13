package com.simprints.infraimages.remote

import com.simprints.infraimages.model.SecuredImageRef

/**
 * An upload result
 *
 * @property image
 *           the image
 * @property status
 *           the outcome of the operation, either successful or failed
 */
data class UploadResult(val image: SecuredImageRef, val status: Status) {

    fun isUploadSuccessful() = status == Status.SUCCESSFUL

    enum class Status {
        SUCCESSFUL,
        FAILED
    }

}
