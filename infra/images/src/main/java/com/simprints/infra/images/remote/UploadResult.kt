package com.simprints.infra.images.remote

import com.simprints.infra.images.model.SecuredImageRef

/**
 * An upload result
 *
 * @property image
 *           the image
 * @property status
 *           the outcome of the operation, either successful or failed
 */
internal data class UploadResult(
    val image: SecuredImageRef,
    val status: Status,
) {
    fun isUploadSuccessful() = status == Status.SUCCESSFUL

    enum class Status {
        SUCCESSFUL,
        FAILED,
    }
}
