package com.simprints.clientapi.clientrequests.extractors

import android.content.Intent
import com.simprints.libsimprints.Constants


abstract class ClientRequestExtractor(private val intent: Intent) {

    open fun getProjectId(): String? = intent.getStringExtra(Constants.SIMPRINTS_PROJECT_ID)

    open fun getUserId(): String? = intent.getStringExtra(Constants.SIMPRINTS_USER_ID)

    open fun getModuleId(): String? = intent.getStringExtra(Constants.SIMPRINTS_MODULE_ID)

    open fun getMetatdata(): String? = intent.getStringExtra(Constants.SIMPRINTS_METADATA)

    // TODO: remove legacy code
    open fun getLegacyApiKey(): String? = intent.getStringExtra(Constants.SIMPRINTS_API_KEY)

    protected fun readString(key: String): String? = intent.getStringExtra(key)

}
