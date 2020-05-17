package com.simprints.clientapi.clientrequests.extractors.odk

import android.content.Intent
import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor

class OdkIdentifyExtractor(
    intent: Intent,
    acceptableExtras: List<String>
) : IdentifyExtractor(intent) {

    override val expectedKeys: List<String> = super.expectedKeys + acceptableExtras

}
