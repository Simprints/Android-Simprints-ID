package com.simprints.clientapi.extensions

import com.simprints.clientapi.Constants
import com.simprints.clientapi.domain.responses.ErrorResponse

internal fun ErrorResponse.isFlowCompletedWithCurrentError(): Boolean =
    when (reason) {
        ErrorResponse.Reason.UNEXPECTED_ERROR,
        ErrorResponse.Reason.DIFFERENT_PROJECT_ID_SIGNED_IN,
        ErrorResponse.Reason.DIFFERENT_USER_ID_SIGNED_IN,
        ErrorResponse.Reason.INVALID_CLIENT_REQUEST,
        ErrorResponse.Reason.INVALID_METADATA,
        ErrorResponse.Reason.INVALID_MODULE_ID,
        ErrorResponse.Reason.INVALID_PROJECT_ID,
        ErrorResponse.Reason.INVALID_SELECTED_ID,
        ErrorResponse.Reason.INVALID_SESSION_ID,
        ErrorResponse.Reason.INVALID_USER_ID,
        ErrorResponse.Reason.INVALID_VERIFY_ID,
        ErrorResponse.Reason.BLUETOOTH_NOT_SUPPORTED,
        ErrorResponse.Reason.GUID_NOT_FOUND_ONLINE -> Constants.RETURN_FOR_FLOW_COMPLETED
        ErrorResponse.Reason.LOGIN_NOT_COMPLETE,
        ErrorResponse.Reason.ROOTED_DEVICE -> Constants.RETURN_FOR_FLOW_NOT_COMPLETED
    }
