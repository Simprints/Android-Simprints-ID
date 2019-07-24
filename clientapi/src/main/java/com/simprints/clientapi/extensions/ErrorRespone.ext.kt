package com.simprints.clientapi.extensions

import com.simprints.clientapi.Constants
import com.simprints.clientapi.domain.responses.ErrorResponse

internal fun ErrorResponse.skipCheckForError(): Boolean =
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
        ErrorResponse.Reason.GUID_NOT_FOUND_ONLINE -> Constants.SKIP_CHECK_VALUE_FOR_COMPLETED_FLOW
        // Atm, if user experiences a recoverable error,
        // he can't leave the app (e.g. pressing Back button).
    }
