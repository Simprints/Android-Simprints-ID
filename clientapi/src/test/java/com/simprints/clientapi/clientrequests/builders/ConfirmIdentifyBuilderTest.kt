package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.requestFactories.ConfirmIdentifyFactory
import com.simprints.clientapi.requestFactories.RequestFactory
import com.simprints.clientapi.simprintsrequests.requests.ClientApiConfirmIdentifyRequest
import org.junit.Assert


class ConfirmIdentifyBuilderTest : SimprintsRequestBuilderTest(ConfirmIdentifyFactory) {

    override fun buildSimprintsRequest_shouldSucceed() {
        super.buildSimprintsRequest_shouldSucceed()

        mockFactory.getBuilder(mockFactory.getMockExtractor()).build().let {
            it as ClientApiConfirmIdentifyRequest
            Assert.assertEquals(it.sessionId, RequestFactory.MOCK_SESSION_ID)
            Assert.assertEquals(it.selectedGuid, RequestFactory.MOCK_SELECTED_GUID)
        }
    }

}
