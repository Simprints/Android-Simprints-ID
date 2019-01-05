package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.requestFactories.RequestFactory.Companion.MOCK_VERIFY_GUID
import com.simprints.clientapi.requestFactories.VerifyRequestFactory
import com.simprints.clientapi.simprintsrequests.VerifyRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyVerifyRequest
import org.junit.Assert


class VerifyBuilderTest : ActionRequestBuilderTest(VerifyRequestFactory) {

    override fun buildSimprintsRequest_shouldSucceed() {
        super.buildSimprintsRequest_shouldSucceed()
        mockFactory.getBuilder(mockFactory.getMockExtractor()).build().let {
            it as VerifyRequest
            Assert.assertEquals(it.verifyGuid, MOCK_VERIFY_GUID)
        }
    }

    override fun buildLegacySimprintsRequest_shouldSucceed() {
        super.buildLegacySimprintsRequest_shouldSucceed()
        mockFactory.getBuilder(mockFactory.getMockExtractor(true)).build().let {
            it as LegacyVerifyRequest
            Assert.assertEquals(it.verifyGuid, MOCK_VERIFY_GUID)
        }
    }

}
