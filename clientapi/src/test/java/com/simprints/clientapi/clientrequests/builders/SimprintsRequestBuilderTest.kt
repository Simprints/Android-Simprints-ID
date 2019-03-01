package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.requestFactories.RequestFactory
import org.junit.Assert
import org.junit.Test


abstract class SimprintsRequestBuilderTest(val mockFactory: RequestFactory) {

    @Test
    open fun buildSimprintsRequest_shouldSucceed() {
        val request = mockFactory.getBuilder(mockFactory.getMockExtractor()).build()

        Assert.assertEquals(request.projectId, RequestFactory.MOCK_PROJECT_ID)
    }

}
