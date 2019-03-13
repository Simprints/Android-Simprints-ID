package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.requestFactories.ConfirmIdentifyFactory
import com.simprints.clientapi.requestFactories.RequestFactory
import com.simprints.clientapi.domain.confirmations.IdentifyConfirmation
import org.junit.Assert

class ConfirmIdentifyBuilderTest : SimprintsRequestBuilderTest(ConfirmIdentifyFactory) {

    override fun buildSimprintsRequest_shouldSucceed() {
        super.buildSimprintsRequest_shouldSucceed()

        mockFactory.getBuilder(mockFactory.getMockExtractor()).build().let {
            it as IdentifyConfirmation
            Assert.assertEquals(it.sessionId, RequestFactory.MOCK_SESSION_ID)
            Assert.assertEquals(it.selectedGuid, RequestFactory.MOCK_SELECTED_GUID)
        }
    }
}
