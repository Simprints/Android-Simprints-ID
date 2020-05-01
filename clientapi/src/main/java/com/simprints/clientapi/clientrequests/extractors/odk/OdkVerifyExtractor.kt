package com.simprints.clientapi.clientrequests.extractors.odk

import android.content.Intent
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor

class OdkVerifyExtractor(intent: Intent, acceptableExtras: List<String>) : VerifyExtractor(intent) {

    override val expectedKeys: List<String> = super.expectedKeys + acceptableExtras

}
