package com.simprints.clientapi.clientrequests.validators

import com.google.gson.Gson
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor


abstract class ClientRequestValidator(private val extractor: ClientRequestExtractor) {

    abstract fun validateClientRequest()

    protected fun hasValidProjectId(): Boolean = !extractor.getProjectId().isNullOrBlank()

    protected fun hasValidModuleId(): Boolean = !extractor.getModuleId().isNullOrBlank()

    protected fun hasValidUserId(): Boolean = !extractor.getUserId().isNullOrBlank()

    // TODO: remove legacy
    protected fun hasValidApiKey(): Boolean = !extractor.getLegacyApiKey().isNullOrBlank()

    protected fun hasMetadata(): Boolean = !extractor.getMetatdata().isNullOrBlank()

    // TODO: inject gson dependency
    protected fun hasValidMetadata(): Boolean = try {
        Gson().fromJson(extractor.getMetatdata(), Any::class.java)
        true
    } catch (ex: com.google.gson.JsonSyntaxException) {
        false
    }

}


