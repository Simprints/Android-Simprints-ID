package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.exceptions.InvalidSelectedIdException
import com.simprints.clientapi.exceptions.InvalidSessionIdException
import com.simprints.clientapi.requestFactories.ConfirmIdentityFactory
import com.simprints.testtools.common.syntax.whenever
import org.junit.Assert
import org.junit.Test

class ConfirmIdentityValidatorTest : AppRequestValidatorTest(ConfirmIdentityFactory) {

    override fun validateClientRequest_shouldFailOnModuleId() {}

    override fun validateClientRequest_shouldFailOnUserId() {}

    @Test
    fun validateClientRequest_shouldFailOnSessionId() {
        val extractor = ConfirmIdentityFactory.getMockExtractor()
        whenever(extractor) { getSessionId() } thenReturn ""

        try {
            ConfirmIdentityFactory.getValidator(extractor).validateClientRequest()
            Assert.fail()
        } catch (ex: Exception) {
            Assert.assertTrue(ex is InvalidSessionIdException)
        }
    }

    @Test
    fun validateClientRequest_shouldFailOnSelectedGuid() {
        val extractor = ConfirmIdentityFactory.getMockExtractor()
        whenever(extractor) { getSelectedGuid() } thenReturn ""

        try {
            ConfirmIdentityFactory.getValidator(extractor).validateClientRequest()
            Assert.fail()
        } catch (ex: Exception) {
            Assert.assertTrue(ex is InvalidSelectedIdException)
        }
    }
}
