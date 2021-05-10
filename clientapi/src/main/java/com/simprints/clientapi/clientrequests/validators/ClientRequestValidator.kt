package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.Constants.PROJECT_ID_LENGTH
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.exceptions.InvalidMetadataException
import com.simprints.clientapi.exceptions.InvalidModuleIdException
import com.simprints.clientapi.exceptions.InvalidProjectIdException
import com.simprints.clientapi.exceptions.InvalidUserIdException
import com.simprints.core.tools.json.JsonHelper


abstract class ClientRequestValidator(private val extractor: ClientRequestExtractor) {

    open fun validateClientRequest() {
        validateProjectId()
        validateUserId()
        validateModuleId()
        validateMetadata()
    }

    protected open fun validateProjectId() {
        if (extractor.getProjectId().isBlank())
            throw InvalidProjectIdException("Missing Project ID")
        else if (extractor.getProjectId().length != PROJECT_ID_LENGTH)
            throw InvalidProjectIdException("Project ID has invalid length")
    }

    protected open fun validateUserId() {
        if (extractor.getUserId().isBlank())
            throw InvalidUserIdException("Missing User ID")
    }

    protected open fun validateModuleId() {
        if (extractor.getModuleId().isBlank())
            throw InvalidModuleIdException("Missing Module ID")
        else if (extractor.getModuleId().contains("|"))
            throw InvalidModuleIdException("Illegal Module ID")
    }

    protected open fun validateMetadata() {
        if (!extractor.getMetadata().isBlank())
            if (!hasValidMetadata())
                throw InvalidMetadataException("Invalid Metadata")
    }

    private fun hasValidMetadata(): Boolean = try {
        JsonHelper.validateJsonOrThrow(extractor.getMetadata())
        true
    } catch (ex: Throwable) {
        false
    }
}


