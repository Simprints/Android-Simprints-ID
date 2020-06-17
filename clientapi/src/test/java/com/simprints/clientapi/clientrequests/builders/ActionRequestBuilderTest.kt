package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.requestFactories.RequestFactory
import org.junit.Assert
import org.junit.Test

abstract class ActionRequestBuilderTest(mockFactory: RequestFactory)
    : SimprintsRequestBuilderTest(mockFactory) {

    @Test
    override fun buildSimprintsRequest_shouldSucceed() {
        super.buildSimprintsRequest_shouldSucceed()
        val request = mockFactory.getBuilder(mockFactory.getMockExtractor()).build()

        Assert.assertEquals(request.projectId, RequestFactory.MOCK_PROJECT_ID)
        Assert.assertEquals(request.userId, RequestFactory.MOCK_USER_ID)
    }
}
