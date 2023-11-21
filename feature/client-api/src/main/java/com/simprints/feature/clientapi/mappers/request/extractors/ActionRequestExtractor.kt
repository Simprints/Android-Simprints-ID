package com.simprints.feature.clientapi.mappers.request.extractors

import android.content.Intent
import com.simprints.feature.clientapi.extensions.extractString
import com.simprints.libsimprints.Constants


internal abstract class ActionRequestExtractor(private val extras: Map<String, Any>) {

    abstract val expectedKeys: List<String>

    protected val keys = listOf(
        Constants.SIMPRINTS_PROJECT_ID,
        Constants.SIMPRINTS_USER_ID,
        Constants.SIMPRINTS_MODULE_ID,
        Constants.SIMPRINTS_METADATA
    )

    open fun getProjectId(): String = extras.extractString(Constants.SIMPRINTS_PROJECT_ID)

    open fun getUserId(): String = extras.extractString(Constants.SIMPRINTS_USER_ID)

    open fun getModuleId(): String = extras.extractString(Constants.SIMPRINTS_MODULE_ID)

    open fun getMetadata(): String = extras.extractString(Constants.SIMPRINTS_METADATA)

    protected open fun Intent.extractString(key: String): String = this.getStringExtra(key) ?: ""

    open fun getUnknownExtras(): List<Pair<String, Any?>> = extras.filter { it.key.isNotBlank() && !expectedKeys.contains(it.key) }.toList()
}
