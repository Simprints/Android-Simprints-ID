package com.simprints.core.tools.extentions

import com.fasterxml.jackson.databind.ObjectMapper
import com.simprints.core.network.NetworkConstants
import org.json.JSONObject
import retrofit2.HttpException

fun Throwable.isCloudRecoverableIssue() =
    this is HttpException && NetworkConstants.httpCodesForRecoverableCloudIssues.contains(this.code())

fun Throwable.isBackendMaitenanceException(): Boolean {
    if (this is HttpException) {
        val ow = ObjectMapper().writer().withDefaultPrettyPrinter()
        val jsonResponse = ow.writeValueAsString(response()?.errorBody())
        val jsonObect = JSONObject(jsonResponse)
        if (response()?.code() == 503 && jsonObect.has("error")) {
            if (jsonObect.getString("error") == "002") {
                return true
            }
        }
    }
    return false
}

fun Throwable.getEstimatedOutage(): Long? {
    return if (this.isBackendMaitenanceException()) {
        val exception = this as HttpException
        val headers = exception.response()?.headers()
        headers?.get("Retry-After")?.toLong()
    } else {
        null
    }
}
