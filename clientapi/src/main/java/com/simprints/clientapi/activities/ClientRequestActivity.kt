package com.simprints.clientapi.activities

import com.simprints.clientapi.clientrequests.extractors.EnrollmentExtractor

interface ClientRequestActivity {

    val enrollmentExtractor: EnrollmentExtractor

}
