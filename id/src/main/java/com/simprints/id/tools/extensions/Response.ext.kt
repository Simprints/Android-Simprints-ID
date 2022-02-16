package com.simprints.id.tools.extensions

import com.fasterxml.jackson.databind.ObjectMapper
import org.json.JSONObject
import retrofit2.Response

fun <T> Response<T>.isBackendMaitenanceException(): Boolean {
    if (this.code() == 503) {
        val ow = ObjectMapper().writer().withDefaultPrettyPrinter()
        val jsonResponse = ow.writeValueAsString(this.errorBody())
        val jsonObect = JSONObject(jsonResponse)
        if (jsonObect.has("error")) {
            if (jsonObect.getString("error") == "002") {
                return true
            }
        }
    }
    return false
}
