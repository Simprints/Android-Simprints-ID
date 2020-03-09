package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.exceptions.InvalidSelectedIdException
import com.simprints.clientapi.exceptions.InvalidSessionIdException
import com.simprints.clientapi.requestFactories.ConfirmIdentityFactory
import io.mockk.every
import org.junit.Assert
import org.junit.Test

class ConfirmIdentityValidatorTest : AppRequestValidatorTest(ConfirmIdentityFactory) {

    override fun validateClientRequest_shouldFailOnModuleId() {}

    override fun validateClientRequest_shouldFailOnUserId() {}

    @Test
    fun validateClientRequest_shouldFailOnSessionId() {
        val extractor = ConfirmIdentityFactory.getMockExtractor()
        every { extractor.getSessionId() } returns ""

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
        every { extractor.getSelectedGuid() } returns ""

        try {
            ConfirmIdentityFactory.getValidator(extractor).validateClientRequest()
            Assert.fail()
        } catch (ex: Exception) {
            Assert.assertTrue(ex is InvalidSelectedIdException)
        }
    }
}
