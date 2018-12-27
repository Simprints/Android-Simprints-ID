package com.simprints.clientapi.clientrequests.validators

import com.google.gson.Gson
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.requests.ClientRequest


abstract class ClientRequestValidator(private val extractor: ClientRequestExtractor) {

    abstract fun validateClientRequest(): ClientRequest

    fun hasValidProjectId(): Boolean = !extractor.getProjectId().isNullOrBlank()

    fun hasValidModuleId(): Boolean = !extractor.getModuleId().isNullOrBlank()

    fun hasValidUserId(): Boolean = !extractor.getUserId().isNullOrBlank()

    // TODO: remove legacy
    fun hasValidApiKey(): Boolean = !extractor.getLegacyApiKey().isNullOrBlank()

    fun hasMetadata(): Boolean = !extractor.getMetatdata().isNullOrBlank()

    // TODO: inject gson dependency
    fun hasValidMetadata(): Boolean = try {
        Gson().fromJson(extractor.getMetatdata(), Any::class.java)
        true
    } catch (ex: com.google.gson.JsonSyntaxException) {
        false
    }

}


