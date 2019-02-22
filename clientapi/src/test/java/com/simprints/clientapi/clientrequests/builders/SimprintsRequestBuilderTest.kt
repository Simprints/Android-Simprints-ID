package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.requestFactories.RequestFactory
import com.simprints.clientapi.requestFactories.RequestFactory.Companion.MOCK_LEGACY_API_KEY
import com.simprints.clientapi.simprintsrequests.ApiVersion
import com.simprints.clientapi.simprintsrequests.requests.legacy.LegacySimprintsIdRequest
import org.junit.Assert
import org.junit.Test


abstract class SimprintsRequestBuilderTest(val mockFactory: RequestFactory) {

    @Test
    open fun buildSimprintsRequest_shouldSucceed() {
        val request = mockFactory.getBuilder(mockFactory.getMockExtractor()).build()

        Assert.assertEquals(request.apiVersion, ApiVersion.V2)
        Assert.assertEquals(request.projectId, RequestFactory.MOCK_PROJECT_ID)
    }

    @Test
    open fun buildLegacySimprintsRequest_shouldSucceed() {
        val extractor = mockFactory.getMockExtractor(withLegacyApiKey = true)
        val request = mockFactory.getBuilder(extractor).build() as LegacySimprintsIdRequest

        Assert.assertEquals(request.apiVersion, ApiVersion.V1)
        Assert.assertEquals(request.legacyApiKey, MOCK_LEGACY_API_KEY)
    }

}
