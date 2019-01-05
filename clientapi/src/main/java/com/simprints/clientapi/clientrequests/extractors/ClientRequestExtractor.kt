package com.simprints.clientapi.clientrequests.extractors

import android.content.Intent
import com.simprints.libsimprints.Constants


abstract class ClientRequestExtractor(private val intent: Intent) {

    open fun getProjectId(): String = intent.extractString(Constants.SIMPRINTS_PROJECT_ID)

    open fun getUserId(): String = intent.extractString(Constants.SIMPRINTS_USER_ID)

    open fun getModuleId(): String = intent.extractString(Constants.SIMPRINTS_MODULE_ID)

    open fun getMetatdata(): String = intent.extractString(Constants.SIMPRINTS_METADATA)

    // TODO: remove legacy code
    open fun getLegacyApiKey(): String = intent.extractString(Constants.SIMPRINTS_API_KEY)

    protected open fun Intent.extractString(key: String): String = this.getStringExtra(key) ?: ""

}
