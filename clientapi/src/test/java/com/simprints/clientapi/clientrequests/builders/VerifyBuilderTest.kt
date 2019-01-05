package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.requestFactories.MockClientRequestFactory.Companion.MOCK_VERIFY_GUID
import com.simprints.clientapi.requestFactories.MockVerifyFactory
import com.simprints.clientapi.simprintsrequests.VerifyRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyVerifyRequest
import org.junit.Assert


class VerifyBuilderTest : ClientRequestBuilderTest(MockVerifyFactory) {

    override fun buildClientRequest_shouldSucceed() {
        super.buildClientRequest_shouldSucceed()
        mockFactory.getBuilder(mockFactory.getMockExtractor()).build().let {
            it as VerifyRequest
            Assert.assertEquals(it.verifyGuid, MOCK_VERIFY_GUID)
        }
    }

    override fun buildLegacyClientRequest_shouldSucceed() {
        super.buildLegacyClientRequest_shouldSucceed()
        mockFactory.getBuilder(mockFactory.getMockExtractor(true)).build().let {
            it as LegacyVerifyRequest
            Assert.assertEquals(it.verifyGuid, MOCK_VERIFY_GUID)
        }
    }

}
