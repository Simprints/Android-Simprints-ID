package com.simprints.id.tools.extensions

import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response

fun <T> Response<T>.isBackendMaitenanceException(): Boolean {
    if (this.code() == 503) {
        val responseJson = this.errorBody().toString()
        val jsonObect = JSONObject(responseJson.substring(responseJson.indexOf("{"), responseJson.lastIndexOf("}") + 1))
        if (jsonObect.has("error")) {
            if (jsonObect.getString("error") == "002") {
                return true
            }
        }
    }
    return false
}

fun <T> Response<T>.getEstimatedOutage(): Long? {
    return if (this.isBackendMaitenanceException()) {
        val headers = this.headers()
        headers.get("Retry-After")?.toLong()
    } else {
        null
    }
}
