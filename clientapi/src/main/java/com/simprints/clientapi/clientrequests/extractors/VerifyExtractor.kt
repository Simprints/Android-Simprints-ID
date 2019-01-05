package com.simprints.clientapi.clientrequests.extractors

import android.content.Intent
import com.simprints.libsimprints.Constants


class VerifyExtractor(val intent: Intent) : ClientRequestExtractor(intent) {

    fun getVerifyGuid(): String = intent.getStringExtra(Constants.SIMPRINTS_VERIFY_GUID)

}
