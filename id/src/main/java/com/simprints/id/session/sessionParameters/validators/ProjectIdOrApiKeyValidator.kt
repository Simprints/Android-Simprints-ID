package com.simprints.id.session.sessionParameters.validators

import com.simprints.id.exceptions.safe.SafeException
import com.simprints.id.session.sessionParameters.SessionParameters

class ProjectIdOrApiKeyValidator(private val errorWhenInvalid: SafeException) : Validator<SessionParameters> {

    override fun validate(value: SessionParameters) {
        // One between ApiKey and Project Id has to have a value
        if (value.apiKey.isEmpty() && value.projectId.isEmpty()) {
            throw errorWhenInvalid
        }
    }
}
