package com.simprints.core.tools.extentions

import com.simprints.core.network.NetworkConstants
import retrofit2.HttpException

fun Throwable.isCloudRecoverableIssue() =
    this is HttpException && NetworkConstants.httpCodesForRecoverableCloudIssues.contains(this.code())

fun Throwable.isBackendMaintenanceException(): Boolean {
    if (this is HttpException && response()?.code() == 503) {
        val jsonResponse = response()?.errorBody()?.string()?.filterNot { it.isWhitespace() }
        return jsonResponse != null && jsonResponse.contains(NetworkConstants.BACKEND_MAINTENANCE_ERROR_STRING)
    }
    return false
}

fun Throwable.getEstimatedOutage(): Long? {
    return if (this.isBackendMaintenanceException()) {
        try {
            val exception = this as HttpException
            val headers = exception.response()?.headers()
            headers?.get(NetworkConstants.HEADER_RETRY_AFTER)?.toLong()
        } catch (e: NumberFormatException) {
            null
        }
    } else {
        null
    }
}
