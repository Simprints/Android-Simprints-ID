package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.requestFactories.RequestFactory
import com.simprints.clientapi.simprintsrequests.requests.ClientApiActionRequest
import com.simprints.clientapi.simprintsrequests.requests.ClientApiBaseRequest
import org.junit.Assert
import org.junit.Test


abstract class ActionRequestBuilderTest(mockFactory: RequestFactory)
    : SimprintsRequestBuilderTest(mockFactory) {

    @Test
    override fun buildSimprintsRequest_shouldSucceed() {
        super.buildSimprintsRequest_shouldSucceed()
        val request = mockFactory.getBuilder(mockFactory.getMockExtractor()).build()
            as ClientApiActionRequest

        Assert.assertTrue(request is ClientApiBaseRequest)
        Assert.assertEquals(request.moduleId, RequestFactory.MOCK_MODULE_ID)
        Assert.assertEquals(request.userId, RequestFactory.MOCK_USER_ID)
        Assert.assertEquals(request.metadata, RequestFactory.MOCK_METADATA)
    }

}

