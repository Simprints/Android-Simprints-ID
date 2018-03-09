package com.simprints.id.sync.contracts

interface UploadContract {
    fun uploadStart(total: Int)
    fun uploadUpdate(remaining: Int)
    fun uploadFinished(remaining: Int)
    fun uploadError(exception: Exception)
}
