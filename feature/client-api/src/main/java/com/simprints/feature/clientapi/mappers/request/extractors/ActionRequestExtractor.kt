package com.simprints.feature.clientapi.mappers.request.extractors

import com.simprints.core.tools.json.JsonHelper
import com.simprints.feature.clientapi.extensions.extractString
import com.simprints.feature.clientapi.models.ClientApiConstants
import com.simprints.libsimprints.Constants
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

internal abstract class ActionRequestExtractor(
    private val extras: Map<String, Any>,
) {
    abstract val expectedKeys: List<String>

    protected val keys = listOf(
        Constants.SIMPRINTS_PROJECT_ID,
        Constants.SIMPRINTS_USER_ID,
        Constants.SIMPRINTS_MODULE_ID,
        Constants.SIMPRINTS_METADATA,
        Constants.SIMPRINTS_LIB_VERSION,
        ClientApiConstants.CALLER_PACKAGE_NAME,
    )

    open fun getProjectId(): String = extras.extractString(Constants.SIMPRINTS_PROJECT_ID)

    open fun getUserId(): String = extras.extractString(Constants.SIMPRINTS_USER_ID)

    open fun getModuleId(): String = extras.extractString(Constants.SIMPRINTS_MODULE_ID)

    open fun getBiometricDataSource(): String = extras.extractString(Constants.SIMPRINTS_BIOMETRIC_DATA_SOURCE)

    open fun getMetadata(): String = extras.extractString(Constants.SIMPRINTS_METADATA)

    fun getSubjectAge(): Int? = try {
        val parsedMetadata =
            JsonHelper.json.decodeFromString<Map<String, JsonElement>>(getMetadata())
        parsedMetadata[Constants.SIMPRINTS_SUBJECT_AGE]
            ?.jsonPrimitive
            ?.intOrNull
    } catch (_: Exception) {
        null
    }

    open fun getUnknownExtras(): Map<String, String?> =
        extras.filter { it.key.isNotBlank() && !expectedKeys.contains(it.key) }.mapValues { (_, value) -> value.toString() }
}
