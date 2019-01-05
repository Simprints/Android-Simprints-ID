package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.requestFactories.ConfirmIdentifyFactory
import com.simprints.clientapi.requestFactories.RequestFactory
import com.simprints.clientapi.simprintsrequests.ConfirmIdentifyRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyConfirmIdentifyRequest
import org.junit.Assert


class ConfirmIdentifyBuilderTest : SimprintsRequestBuilderTest(ConfirmIdentifyFactory) {

    override fun buildSimprintsRequest_shouldSucceed() {
        super.buildSimprintsRequest_shouldSucceed()

        mockFactory.getBuilder(mockFactory.getMockExtractor()).build().let {
            it as ConfirmIdentifyRequest
            Assert.assertEquals(it.sessionId, RequestFactory.MOCK_SESSION_ID)
            Assert.assertEquals(it.selectedGuid, RequestFactory.MOCK_SELECTED_GUID)
        }
    }

    override fun buildLegacySimprintsRequest_shouldSucceed() {
        super.buildLegacySimprintsRequest_shouldSucceed()

        mockFactory.getBuilder(mockFactory.getMockExtractor(true)).build().let {
            it as LegacyConfirmIdentifyRequest
            Assert.assertEquals(it.sessionId, RequestFactory.MOCK_SESSION_ID)
            Assert.assertEquals(it.selectedGuid, RequestFactory.MOCK_SELECTED_GUID)
        }
    }


}
