package com.simprints.clientapi.validators

import android.content.Intent
import com.google.gson.Gson
import com.simprints.libsimprints.Constants

abstract class ClientRequestValidator(val intent: Intent) {

    abstract fun validateClientRequest()

    fun hasValidProjectId(): Boolean =
        !intent.getStringExtra(Constants.SIMPRINTS_PROJECT_ID).isNullOrBlank()

    fun hasValidModuleId(): Boolean =
        !intent.getStringExtra(Constants.SIMPRINTS_MODULE_ID).isNullOrBlank()

    fun hasValidUserId(): Boolean =
        !intent.getStringExtra(Constants.SIMPRINTS_USER_ID).isNullOrBlank()

    // TODO: remove legacy
    fun hasValidApiKey(): Boolean =
        !intent.getStringExtra(Constants.SIMPRINTS_API_KEY).isNullOrBlank()

    fun hasMetadata(): Boolean =
        !intent.getStringExtra(Constants.SIMPRINTS_METADATA).isNullOrBlank()

    // TODO: inject gson dependency
    fun hasValidMetadata(): Boolean = try {
        Gson().fromJson(intent.getStringExtra(Constants.SIMPRINTS_METADATA), Any::class.java)
        true
    } catch (ex: com.google.gson.JsonSyntaxException) {
        false
    }

}


