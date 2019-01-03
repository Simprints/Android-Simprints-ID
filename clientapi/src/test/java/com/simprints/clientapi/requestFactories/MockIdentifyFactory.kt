package com.simprints.clientapi.requestFactories

import com.simprints.clientapi.clientrequests.builders.IdentifyBuilder
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.validators.IdentifyValidator
import org.mockito.Mockito


object MockIdentifyFactory : MockClientRequestFactory() {

    override fun getValidator(extractor: ClientRequestExtractor): IdentifyValidator =
        IdentifyValidator(extractor as IdentifyExtractor)

    override fun getBuilder(extractor: ClientRequestExtractor): IdentifyBuilder =
        IdentifyBuilder(extractor as IdentifyExtractor, getValidator(extractor))

    override fun getMockExtractor(withLegacyApiKey: Boolean): IdentifyExtractor {
        val mockIdentifyExtractor = Mockito.mock(IdentifyExtractor::class.java)
        setMockDefaultExtractor(mockIdentifyExtractor, withLegacyApiKey)
        return mockIdentifyExtractor
    }

}
