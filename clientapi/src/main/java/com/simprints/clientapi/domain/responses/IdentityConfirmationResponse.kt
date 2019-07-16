package com.simprints.clientapi.domain.responses

import com.simprints.moduleapi.app.responses.IAppIdentityConfirmationResponse

data class IdentityConfirmationResponse(val identificationOutcome: Boolean) {

    constructor(response: IAppIdentityConfirmationResponse): this(response.identificationOutcome)

}
