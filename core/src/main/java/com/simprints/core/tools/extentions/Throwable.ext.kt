package com.simprints.core.tools.extentions

import androidx.annotation.Keep
import com.simprints.core.network.NetworkConstants
import com.simprints.core.tools.json.JsonHelper
import retrofit2.HttpException

fun Throwable.isCloudRecoverableIssue() =
    this is HttpException && NetworkConstants.httpCodesForRecoverableCloudIssues.contains(this.code())

fun Throwable.isBackendMaintenanceException(): Boolean {
    if (this is HttpException && response()?.code() == 503) {
        return try {
            val error = response()?.errorBody()?.string()?.let { JsonHelper.fromJson<ApiError>(it).error }
            error != null && error == NetworkConstants.BACKEND_MAINTENANCE_ERROR_STRING
        } catch (e: Throwable) {
            false
        }
    }
    return false
}

fun Throwable.getEstimatedOutage(): Long? {
    return try {
        val exception = this as HttpException
        val headers = exception.response()?.headers()
        headers?.get(NetworkConstants.HEADER_RETRY_AFTER)?.toLong() ?: 0L
    } catch (e: Throwable) {
        null
    }
}

@Keep
data class ApiError(val error: String)
