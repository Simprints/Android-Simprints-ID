package com.simprints.core.tools.extentions

import com.simprints.core.network.NetworkConstants
import org.json.JSONObject
import retrofit2.HttpException

fun Throwable.isCloudRecoverableIssue() =
    this is HttpException && NetworkConstants.httpCodesForRecoverableCloudIssues.contains(this.code())

fun Throwable.isBackendMaitenanceException(): Boolean {
    if (this is HttpException) {
        val responseJson = response()?.errorBody().toString()
        val jsonObect = JSONObject(responseJson.substring(responseJson.indexOf("{"), responseJson.lastIndexOf("}") + 1))
        if (response()?.code() == 503 && jsonObect.has("error")) {
            if (jsonObect.getString("error") == "002") {
                return true
            }
        }
    }
    return false
}
