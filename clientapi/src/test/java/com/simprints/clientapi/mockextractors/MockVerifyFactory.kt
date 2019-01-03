package com.simprints.clientapi.mockextractors

import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.clientrequests.validators.ClientRequestValidator
import com.simprints.clientapi.clientrequests.validators.VerifyValidator
import org.mockito.Mockito

object MockVerifyFactory : MockClientRequestFactory() {

    override fun getValidator(extractor: ClientRequestExtractor): ClientRequestValidator =
        VerifyValidator(extractor as VerifyExtractor)

    override fun getValidMockExtractor(): VerifyExtractor {
        val mockVerifyExtractor = Mockito.mock(VerifyExtractor::class.java)
        setMockDefaultExtractor(mockVerifyExtractor)
        Mockito.`when`(mockVerifyExtractor.getVerifyGuid()).thenReturn(MockClientRequestFactory.MOCK_VERIFY_GUID)
        return mockVerifyExtractor
    }

}
