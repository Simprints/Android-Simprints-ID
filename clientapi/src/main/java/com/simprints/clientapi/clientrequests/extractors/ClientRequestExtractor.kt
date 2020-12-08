package com.simprints.clientapi.clientrequests.extractors

import android.content.Intent
import com.simprints.clientapi.extensions.toMap
import com.simprints.libsimprints.Constants


abstract class ClientRequestExtractor(private val intent: Intent) {

    abstract val expectedKeys: List<String>

    protected val keys = listOf(
        Constants.SIMPRINTS_PROJECT_ID,
        Constants.SIMPRINTS_USER_ID,
        Constants.SIMPRINTS_MODULE_ID,
        Constants.SIMPRINTS_METADATA
    )

    open fun getProjectId(): String = intent.extractString(Constants.SIMPRINTS_PROJECT_ID)

    open fun getUserId(): String = intent.extractString(Constants.SIMPRINTS_USER_ID)

    open fun getModuleId(): String = intent.extractString(Constants.SIMPRINTS_MODULE_ID)

    open fun getMetadata(): String = intent.extractString(Constants.SIMPRINTS_METADATA)

    protected open fun Intent.extractString(key: String): String = this.getStringExtra(key) ?: ""

    open fun getUnknownExtras(): Map<String, Any?> =
        (intent.extras?.toMap()?.filter { it.key.isNotBlank() && !expectedKeys.contains(it.key) }
            ?: emptyMap())
}
