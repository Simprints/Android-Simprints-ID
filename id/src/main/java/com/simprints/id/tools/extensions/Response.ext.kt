package com.simprints.id.tools.extensions

import com.simprints.id.exceptions.safe.BackendMaintenanceException
import com.simprints.id.exceptions.safe.SimprintsInternalServerException
import org.json.JSONObject
import retrofit2.Response

fun <T> Response<T>.checkForMaintenanceAndThrow(): Nothing {
    if (this.code() == 503) {
        val responseJson = this.errorBody().toString()
        val jsonObect = JSONObject(responseJson.substring(responseJson.indexOf("{"), responseJson.lastIndexOf("}") + 1))
        if (jsonObect.has("error")) {
            if (jsonObect.getString("error") == "002") {
                throw BackendMaintenanceException()
            }
        }
    }
    throw SimprintsInternalServerException()
}
