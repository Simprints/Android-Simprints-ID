package com.simprints.clientapi.clientrequests.extractors

import android.content.Intent


open class EnrolExtractor(intent: Intent) : ClientRequestExtractor(intent) {

    override val expectedKeys = super.keys

}
