package com.simprints.clientapi.mockextractors

import com.simprints.clientapi.clientrequests.builders.EnrollmentBuilder
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.extractors.EnrollmentExtractor
import com.simprints.clientapi.clientrequests.validators.EnrollmentValidator
import org.mockito.Mockito

object MockEnrollmentFactory : MockClientRequestFactory() {

    override fun getBuilder(extractor: ClientRequestExtractor): EnrollmentBuilder =
        EnrollmentBuilder(extractor as EnrollmentExtractor, getValidator(extractor))

    override fun getValidator(extractor: ClientRequestExtractor): EnrollmentValidator =
        EnrollmentValidator(extractor as EnrollmentExtractor)

    override fun getValidMockExtractor(): EnrollmentExtractor {
        val mockEnrollmentExtractor = Mockito.mock(EnrollmentExtractor::class.java)
        setMockDefaultExtractor(mockEnrollmentExtractor)
        return mockEnrollmentExtractor
    }

}
