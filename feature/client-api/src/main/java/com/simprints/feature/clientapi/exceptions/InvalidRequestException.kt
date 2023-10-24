package com.simprints.feature.clientapi.exceptions

import com.simprints.feature.clientapi.models.ClientApiError

internal class InvalidRequestException(
    message: String = "",
    val error: ClientApiError = ClientApiError.INVALID_STATE_FOR_INTENT_ACTION,
) : RuntimeException(message)
