package com.simprints.clientapi.validators

import android.content.Intent
import com.simprints.libsimprints.Constants

abstract class CalloutValidator(val intent: Intent) {

    abstract fun validateClientRequest()

    fun hasValidProjectId(): Boolean =
        !intent.getStringExtra(Constants.SIMPRINTS_PROJECT_ID).isNullOrBlank()

    fun hasValidApiKey(): Boolean =
        !intent.getStringExtra(Constants.SIMPRINTS_API_KEY).isNullOrBlank()

    fun hasValidModuleId(): Boolean =
        !intent.getStringExtra(Constants.SIMPRINTS_MODULE_ID).isNullOrBlank()

    fun hasValidUserId(): Boolean =
        !intent.getStringExtra(Constants.SIMPRINTS_USER_ID).isNullOrBlank()

}
