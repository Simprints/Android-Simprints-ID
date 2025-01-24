package com.simprints.feature.clientapi.mappers.request.validators

import com.simprints.core.tools.json.JsonHelper
import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.extractors.ActionRequestExtractor
import com.simprints.feature.clientapi.models.ClientApiError

internal abstract class RequestActionValidator(
    private val extractor: ActionRequestExtractor,
) {
    open fun validate() {
        validateProjectId()
        validateUserId()
        validateModuleId()
        validateMetadata()
    }

    protected open fun validateProjectId() {
        if (extractor.getProjectId().isBlank()) {
            throw InvalidRequestException("Missing Project ID")
        } else if (extractor.getProjectId().length != PROJECT_ID_LENGTH) {
            throw InvalidRequestException("Project ID has invalid length", ClientApiError.INVALID_PROJECT_ID)
        }
    }

    protected open fun validateUserId() {
        if (extractor.getUserId().isBlank()) {
            throw InvalidRequestException("Missing User ID", ClientApiError.INVALID_USER_ID)
        }
    }

    protected open fun validateModuleId() {
        if (extractor.getModuleId().isBlank()) {
            throw InvalidRequestException("Missing Module ID", ClientApiError.INVALID_MODULE_ID)
        } else if (extractor.getModuleId().contains("|")) {
            throw InvalidRequestException("Illegal Module ID", ClientApiError.INVALID_MODULE_ID)
        }
    }

    protected open fun validateMetadata() {
        if (extractor.getMetadata().isNotBlank()) {
            if (!hasValidMetadata()) {
                throw InvalidRequestException("Invalid Metadata", ClientApiError.INVALID_METADATA)
            }
        }
    }

    private fun hasValidMetadata(): Boolean = try {
        JsonHelper.validateJsonOrThrow(extractor.getMetadata())
        true
    } catch (_: Throwable) {
        false
    }

    companion object {
        private const val PROJECT_ID_LENGTH = 20
    }
}
