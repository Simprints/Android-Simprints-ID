package com.simprints.clientapi.clientrequests.extractors.odk

import android.content.Intent
import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor

class OdkEnrolExtractor(intent: Intent, acceptableExtras: List<String>) : EnrollExtractor(intent) {

    override val expectedKeys: List<String> = super.expectedKeys + acceptableExtras

}
