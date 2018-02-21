package com.simprints.id.domain.sessionParameters.validators

import com.simprints.id.domain.sessionParameters.SessionParameters

class ProjectIdOrApiKeyValidator(private val errorWhenInvalid: Error) : Validator<SessionParameters> {

    override fun validate(value: SessionParameters) {
        // One between ApiKey and Project Id has to have a value
        if (value.apiKey.isEmpty() && value.projectId.isEmpty()) {
            throw errorWhenInvalid
        }
    }
}
