package com.simprints.core.tools.extentions

import com.simprints.core.network.NetworkConstants
import retrofit2.HttpException

fun Throwable.isCloudRecoverableIssue() =
    this is HttpException && NetworkConstants.httpCodesForRecoverableCloudIssues.contains(this.code())

fun Throwable.isBackendMaitenanceException(): Boolean {
    if (this is HttpException && response()?.code() == 503) {
        val jsonReseponse = response()?.errorBody()?.string()?.filterNot { it.isWhitespace() }
        return jsonReseponse != null && jsonReseponse.contains(NetworkConstants.BACKEND_MAINTENANCE_ERROR_STRING)
    }
    return false
}

fun Throwable.getEstimatedOutage(): Long? {
    return if (this.isBackendMaitenanceException()) {
        try {
            val exception = this as HttpException
            val headers = exception.response()?.headers()
            headers?.get("Retry-After")?.toLong()
        } catch (e: NumberFormatException) {
            null
        }
    } else {
        null
    }
}
