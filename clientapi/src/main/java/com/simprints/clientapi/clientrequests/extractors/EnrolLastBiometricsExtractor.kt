package com.simprints.clientapi.clientrequests.extractors

import android.content.Intent
import com.simprints.libsimprints.Constants

class EnrolLastBiometricsExtractor(val intent: Intent) : ClientRequestExtractor(intent) {

    fun getSessionId(): String = intent.extractString(Constants.SIMPRINTS_SESSION_ID)

    override val expectedKeys = super.keys + listOf(Constants.SIMPRINTS_SESSION_ID)

}
