package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.requestFactories.RequestFactory
import com.simprints.clientapi.simprintsrequests.SimprintsActionRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacySimprintsActionRequest
import org.junit.Assert
import org.junit.Test

abstract class ActionRequestBuilderTest(mockFactory: RequestFactory)
    : SimprintsRequestBuilderTest(mockFactory) {

    @Test
    override fun buildSimprintsRequest_shouldSucceed() {
        super.buildSimprintsRequest_shouldSucceed()
        val request = mockFactory.getBuilder(mockFactory.getMockExtractor()).build()
            as SimprintsActionRequest

        Assert.assertEquals(request.moduleId, RequestFactory.MOCK_MODULE_ID)
        Assert.assertEquals(request.userId, RequestFactory.MOCK_USER_ID)
        Assert.assertEquals(request.metadata, RequestFactory.MOCK_METADATA)
    }

    @Test
    override fun buildLegacySimprintsRequest_shouldSucceed() {
        super.buildLegacySimprintsRequest_shouldSucceed()
        val extractor = mockFactory.getMockExtractor(withLegacyApiKey = true)
        val request = mockFactory.getBuilder(extractor).build() as LegacySimprintsActionRequest

        Assert.assertEquals(request.moduleId, RequestFactory.MOCK_MODULE_ID)
        Assert.assertEquals(request.userId, RequestFactory.MOCK_USER_ID)
        Assert.assertEquals(request.metadata, RequestFactory.MOCK_METADATA)
    }
}

