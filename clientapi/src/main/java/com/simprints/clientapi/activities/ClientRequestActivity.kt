package com.simprints.clientapi.activities

import com.simprints.clientapi.clientrequests.extractors.EnrollmentExtractor
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest

interface ClientRequestActivity {

    val enrollmentExtractor: EnrollmentExtractor

    val verifyExtractor: VerifyExtractor

    fun sendSimprintsRequest(request: SimprintsIdRequest)

}
