package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.requests.ClientVerifyRequest
import com.simprints.clientapi.clientrequests.requests.legacy.LegacyClientVerifyRequest
import com.simprints.clientapi.requestFactories.MockClientRequestFactory.Companion.MOCK_VERIFY_GUID
import com.simprints.clientapi.requestFactories.MockVerifyFactory
import org.junit.Assert


class VerifyBuilderTest : ClientRequestBuilderTest(MockVerifyFactory) {

    override fun buildClientRequest_shouldSucceed() {
        super.buildClientRequest_shouldSucceed()
        mockFactory.getBuilder(mockFactory.getMockExtractor()).build().let {
            it as ClientVerifyRequest
            Assert.assertEquals(it.verifyGuid, MOCK_VERIFY_GUID)
        }
    }

    override fun buildLegacyClientRequest_shouldSucceed() {
        super.buildLegacyClientRequest_shouldSucceed()
        mockFactory.getBuilder(mockFactory.getMockExtractor(true)).build().let {
            it as LegacyClientVerifyRequest
            Assert.assertEquals(it.verifyGuid, MOCK_VERIFY_GUID)
        }
    }

}
