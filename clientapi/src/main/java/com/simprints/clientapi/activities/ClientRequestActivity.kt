package com.simprints.clientapi.activities

import com.simprints.clientapi.clientrequests.extractors.EnrollmentExtractor
import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest

interface ClientRequestActivity {

    val enrollmentExtractor: EnrollmentExtractor

    fun sendSimprintsRequest(request: SimprintsIdRequest)

}
