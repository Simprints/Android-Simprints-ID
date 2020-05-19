package com.simprints.clientapi.clientrequests.extractors.odk

import android.content.Intent
import com.simprints.clientapi.clientrequests.extractors.EnrolExtractor

class OdkEnrolExtractor(intent: Intent, acceptableExtras: List<String>) : EnrolExtractor(intent) {

    override val expectedKeys: List<String> = super.expectedKeys + acceptableExtras

}
