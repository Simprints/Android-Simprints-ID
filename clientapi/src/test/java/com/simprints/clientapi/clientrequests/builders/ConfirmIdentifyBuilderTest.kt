package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.requestFactories.ConfirmIdentityFactory
import com.simprints.clientapi.requestFactories.RequestFactory
import com.simprints.clientapi.domain.requests.confirmations.IdentityConfirmation
import org.junit.Assert

class ConfirmIdentifyBuilderTest : SimprintsRequestBuilderTest(ConfirmIdentityFactory) {

    override fun buildSimprintsRequest_shouldSucceed() {
        super.buildSimprintsRequest_shouldSucceed()

        mockFactory.getBuilder(mockFactory.getMockExtractor()).build().let {
            it as IdentityConfirmation
            Assert.assertEquals(it.sessionId, RequestFactory.MOCK_SESSION_ID)
            Assert.assertEquals(it.selectedGuid, RequestFactory.MOCK_SELECTED_GUID)
        }
    }
}
