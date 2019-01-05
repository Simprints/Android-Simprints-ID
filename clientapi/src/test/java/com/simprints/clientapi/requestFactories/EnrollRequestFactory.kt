package com.simprints.clientapi.requestFactories

import com.simprints.clientapi.clientrequests.builders.EnrollBuilder
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor
import com.simprints.clientapi.clientrequests.validators.EnrollValidator
import org.mockito.Mockito

object EnrollRequestFactory : RequestFactory() {

    override fun getBuilder(extractor: ClientRequestExtractor): EnrollBuilder =
        EnrollBuilder(extractor as EnrollExtractor, getValidator(extractor))

    override fun getValidator(extractor: ClientRequestExtractor): EnrollValidator =
        EnrollValidator(extractor as EnrollExtractor)

    override fun getMockExtractor(withLegacyApiKey: Boolean): EnrollExtractor {
        val mockEnrollmentExtractor = Mockito.mock(EnrollExtractor::class.java)
        setMockDefaultExtractor(mockEnrollmentExtractor, withLegacyApiKey)
        return mockEnrollmentExtractor
    }


}
