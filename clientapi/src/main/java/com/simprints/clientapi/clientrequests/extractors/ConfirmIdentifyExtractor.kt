package com.simprints.clientapi.clientrequests.extractors

import android.content.Intent
import com.simprints.libsimprints.Constants.SIMPRINTS_SELECTED_GUID
import com.simprints.libsimprints.Constants.SIMPRINTS_SESSION_ID


class ConfirmIdentifyExtractor(val intent: Intent) : ClientRequestExtractor(intent) {

    fun getSessionId(): String = intent.extractString(SIMPRINTS_SESSION_ID)

    fun getSelectedGuid(): String = intent.extractString(SIMPRINTS_SELECTED_GUID)

}
