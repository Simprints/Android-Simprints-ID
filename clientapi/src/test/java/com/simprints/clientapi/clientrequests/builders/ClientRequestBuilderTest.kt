package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.requests.ApiVersion
import com.simprints.clientapi.clientrequests.requests.legacy.LegacyClientRequest
import com.simprints.clientapi.requestFactories.MockClientRequestFactory
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito


abstract class ClientRequestBuilderTest(private val mockFactory: MockClientRequestFactory) {

    @Test
    fun buildClientRequest_shouldSucceed() {
        val request = mockFactory.getBuilder(mockFactory.getValidMockExtractor()).build()

        Assert.assertEquals(request.apiVersion, ApiVersion.V2)
        Assert.assertEquals(request.projectId, MockClientRequestFactory.MOCK_PROJECT_ID)
        Assert.assertEquals(request.moduleId, MockClientRequestFactory.MOCK_MODULE_ID)
        Assert.assertEquals(request.userId, MockClientRequestFactory.MOCK_USER_ID)
        Assert.assertEquals(request.metadata, MockClientRequestFactory.MOCK_METADATA)
    }

    @Test
    fun buildLegacyClientRequest() {
        val extractor = mockFactory.getValidMockExtractor()
        Mockito.`when`(extractor.getLegacyApiKey()).thenReturn("API_KEY")
        val request = mockFactory.getBuilder(extractor).build() as LegacyClientRequest

        Assert.assertEquals(request.apiVersion, ApiVersion.V1)
        Assert.assertEquals(request.legacyApiKey, "API_KEY")
        Assert.assertEquals(request.moduleId, MockClientRequestFactory.MOCK_MODULE_ID)
        Assert.assertEquals(request.userId, MockClientRequestFactory.MOCK_USER_ID)
        Assert.assertEquals(request.metadata, MockClientRequestFactory.MOCK_METADATA)
    }

}
