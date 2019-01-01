package com.simprints.clientapi.clientrequests.validators

import com.google.gson.Gson
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.exceptions.InvalidMetadataException
import com.simprints.clientapi.exceptions.InvalidModuleIdException
import com.simprints.clientapi.exceptions.InvalidProjectIdException
import com.simprints.clientapi.exceptions.InvalidUserIdException


abstract class ClientRequestValidator(private val extractor: ClientRequestExtractor) {

    abstract fun validateClientRequest()

    protected open fun validateProjectIdOrLegacyApiKey() = try {
        validateProjectId()
    } catch (ex: InvalidProjectIdException) {
        validateLegacyApiKey()
    }

    protected open fun validateProjectId() {
        if (extractor.getProjectId().isNullOrBlank())
            throw InvalidProjectIdException("Missing Project ID")
    }

    protected open fun validateUserId() {
        if (extractor.getUserId().isNullOrBlank())
            throw InvalidUserIdException("Missing User ID")
    }

    protected open fun validateModuleId() {
        if (extractor.getModuleId().isNullOrBlank())
            throw InvalidModuleIdException("Missing Module ID")
        else if (extractor.getModuleId()!!.contains("|"))
            throw InvalidModuleIdException("Illegal Module ID")
    }

    // TODO: remove legacy
    protected open fun validateLegacyApiKey() {
        if (extractor.getLegacyApiKey().isNullOrBlank())
            throw InvalidProjectIdException("Missing Project ID or Legacy API Key")
    }

    protected open fun validateMetadata() {
        if (!extractor.getMetatdata().isNullOrBlank())
            if (!hasValidMetadata())
                throw InvalidMetadataException("Invalid Metadata")
    }

    // TODO: inject gson dependency
    private fun hasValidMetadata(): Boolean = try {
        Gson().fromJson(extractor.getMetatdata(), Any::class.java)
        true
    } catch (ex: com.google.gson.JsonSyntaxException) {
        false
    }

}


