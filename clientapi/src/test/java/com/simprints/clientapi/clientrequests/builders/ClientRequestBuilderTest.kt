package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.requests.ApiVersion
import com.simprints.clientapi.clientrequests.requests.legacy.LegacyClientRequest
import com.simprints.clientapi.requestFactories.MockClientRequestFactory
import com.simprints.clientapi.requestFactories.MockClientRequestFactory.Companion.MOCK_LEGACY_API_KEY
import org.junit.Assert
import org.junit.Test


abstract class ClientRequestBuilderTest(val mockFactory: MockClientRequestFactory) {

    @Test
    open fun buildClientRequest_shouldSucceed() {
        val request = mockFactory.getBuilder(mockFactory.getMockExtractor()).build()

        Assert.assertEquals(request.apiVersion, ApiVersion.V2)
        Assert.assertEquals(request.projectId, MockClientRequestFactory.MOCK_PROJECT_ID)
        Assert.assertEquals(request.moduleId, MockClientRequestFactory.MOCK_MODULE_ID)
        Assert.assertEquals(request.userId, MockClientRequestFactory.MOCK_USER_ID)
        Assert.assertEquals(request.metadata, MockClientRequestFactory.MOCK_METADATA)
    }

    @Test
    open fun buildLegacyClientRequest_shouldSucceed() {
        val extractor = mockFactory.getMockExtractor(withLegacyApiKey = true)
        val request = mockFactory.getBuilder(extractor).build() as LegacyClientRequest

        Assert.assertEquals(request.apiVersion, ApiVersion.V1)
        Assert.assertEquals(request.legacyApiKey, MOCK_LEGACY_API_KEY)
        Assert.assertEquals(request.moduleId, MockClientRequestFactory.MOCK_MODULE_ID)
        Assert.assertEquals(request.userId, MockClientRequestFactory.MOCK_USER_ID)
        Assert.assertEquals(request.metadata, MockClientRequestFactory.MOCK_METADATA)
    }

}
