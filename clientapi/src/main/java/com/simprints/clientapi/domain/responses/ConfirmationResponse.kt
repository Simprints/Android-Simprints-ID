package com.simprints.clientapi.domain.responses

import com.simprints.moduleapi.app.responses.IAppConfirmationResponse

data class ConfirmationResponse(val identificationOutcome: Boolean) {

    constructor(response: IAppConfirmationResponse): this(response.identificationOutcome)

}
