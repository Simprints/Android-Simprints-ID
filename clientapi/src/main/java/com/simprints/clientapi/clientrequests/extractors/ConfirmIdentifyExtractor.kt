package com.simprints.clientapi.clientrequests.extractors

import android.content.Intent
import com.simprints.libsimprints.Constants.SIMPRINTS_SELECTED_GUID
import com.simprints.libsimprints.Constants.SIMPRINTS_SESSION_ID


class ConfirmIdentifyExtractor(val intent: Intent) : ClientRequestExtractor(intent) {

    fun getSessionId(): String = intent.extractString(SIMPRINTS_SESSION_ID)

    fun getSelectedGuid(): String = intent.extractString(SIMPRINTS_SELECTED_GUID)

    override val expectedKeys: List<String> =
        super.keys + listOf(SIMPRINTS_SESSION_ID, SIMPRINTS_SELECTED_GUID)

}
