package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.requestFactories.RequestFactory.Companion.MOCK_VERIFY_GUID
import com.simprints.clientapi.requestFactories.VerifyRequestFactory
import com.simprints.clientapi.simprintsrequests.requests.ClientApiVerifyRequest
import org.junit.Assert


class VerifyBuilderTest : ActionRequestBuilderTest(VerifyRequestFactory) {

    override fun buildSimprintsRequest_shouldSucceed() {
        super.buildSimprintsRequest_shouldSucceed()
        mockFactory.getBuilder(mockFactory.getMockExtractor()).build().let {
            it as ClientApiVerifyRequest
            Assert.assertEquals(it.verifyGuid, MOCK_VERIFY_GUID)
        }
    }

}
