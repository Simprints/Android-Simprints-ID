package com.simprints.clientapi.activities.commcare

import com.simprints.clientapi.Constants.SKIP_CHECK_VALUE_FOR_COMPLETED_FLOW
import com.simprints.clientapi.domain.responses.ErrorResponse

internal fun ErrorResponse.skipCheckForError(): Boolean =

    if (this.reason == ErrorResponse.Reason.LOGIN_NOT_COMPLETE) {
        !SKIP_CHECK_VALUE_FOR_COMPLETED_FLOW
    } else if (this.isUnrecoverableError()) {
        SKIP_CHECK_VALUE_FOR_COMPLETED_FLOW
    } else {
        !SKIP_CHECK_VALUE_FOR_COMPLETED_FLOW
    }
