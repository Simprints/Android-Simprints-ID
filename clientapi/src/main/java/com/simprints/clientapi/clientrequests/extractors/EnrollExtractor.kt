package com.simprints.clientapi.clientrequests.extractors

import android.content.Intent


class EnrollExtractor(intent: Intent) : ClientRequestExtractor(intent) {

    override val expectedKeys: List<String> = super.keys

}
