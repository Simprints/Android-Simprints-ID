package com.simprints.clientapi.mockextractors

import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.extractors.EnrollmentExtractor
import com.simprints.clientapi.clientrequests.validators.EnrollmentValidator
import org.mockito.Mockito

object MockEnrollmentFactory : MockClientRequestFactory() {

    override fun getValidator(extractor: ClientRequestExtractor): EnrollmentValidator =
        EnrollmentValidator(extractor as EnrollmentExtractor)

    override fun getValidMockExtractor(): EnrollmentExtractor {
        val mockEnrollmentExtractor = Mockito.mock(EnrollmentExtractor::class.java)
        setMockDefaultExtractor(mockEnrollmentExtractor)
        return mockEnrollmentExtractor
    }

}
