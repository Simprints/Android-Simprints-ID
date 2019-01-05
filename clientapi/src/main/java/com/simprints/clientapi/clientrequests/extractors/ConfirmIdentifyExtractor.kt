package com.simprints.clientapi.clientrequests.extractors

import android.content.Intent
import com.simprints.libsimprints.Constants.SIMPRINTS_SELECTED_GUID
import com.simprints.libsimprints.Constants.SIMPRINTS_SESSION_ID


class ConfirmIdentifyExtractor(val intent: Intent) : ClientRequestExtractor(intent) {

    fun getSessionId(): String = intent.getStringExtra(SIMPRINTS_SESSION_ID)

    fun getSelectedGuid(): String = intent.getStringExtra(SIMPRINTS_SELECTED_GUID)

}
