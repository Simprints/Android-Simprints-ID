package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.requestFactories.RequestFactory
import com.simprints.clientapi.simprintsrequests.ApiVersion
import org.junit.Assert
import org.junit.Test


abstract class SimprintsRequestBuilderTest(val mockFactory: RequestFactory) {

    @Test
    open fun buildSimprintsRequest_shouldSucceed() {
        val request = mockFactory.getBuilder(mockFactory.getMockExtractor()).build()

        Assert.assertEquals(request.apiVersion, ApiVersion.V2)
        Assert.assertEquals(request.projectId, RequestFactory.MOCK_PROJECT_ID)
    }

}
