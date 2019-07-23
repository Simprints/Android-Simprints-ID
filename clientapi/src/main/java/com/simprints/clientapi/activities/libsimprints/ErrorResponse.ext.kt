package com.simprints.clientapi.activities.libsimprints

import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.clientapi.domain.responses.ErrorResponse.Reason.*
import com.simprints.libsimprints.Constants.*

internal fun ErrorResponse.Reason.libSimprintsResultCode() =
    when(this) {
        DIFFERENT_PROJECT_ID_SIGNED_IN -> SIMPRINTS_INVALID_PROJECT_ID
        DIFFERENT_USER_ID_SIGNED_IN -> SIMPRINTS_INVALID_USER_ID
        GUID_NOT_FOUND_ONLINE -> SIMPRINTS_VERIFY_GUID_NOT_FOUND_ONLINE
        UNEXPECTED_ERROR -> SIMPRINTS_CANCELLED //TODO: should we update LibSimprints with the missing errors?
        BLUETOOTH_NOT_SUPPORTED -> SIMPRINTS_CANCELLED
        INVALID_CLIENT_REQUEST -> SIMPRINTS_INVALID_INTENT_ACTION
        INVALID_METADATA -> SIMPRINTS_INVALID_METADATA
        INVALID_MODULE_ID -> SIMPRINTS_INVALID_MODULE_ID
        INVALID_PROJECT_ID -> SIMPRINTS_INVALID_PROJECT_ID
        INVALID_SELECTED_ID -> SIMPRINTS_CANCELLED
        INVALID_SESSION_ID -> SIMPRINTS_CANCELLED
        INVALID_USER_ID -> SIMPRINTS_INVALID_USER_ID
        INVALID_VERIFY_ID -> SIMPRINTS_INVALID_VERIFY_GUID
    }
