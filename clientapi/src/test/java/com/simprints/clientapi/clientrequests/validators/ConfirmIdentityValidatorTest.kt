package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.exceptions.InvalidSelectedIdException
import com.simprints.clientapi.exceptions.InvalidSessionIdException
import com.simprints.clientapi.requestFactories.ConfirmIdentityFactory
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.every
import org.junit.Test

class ConfirmIdentityValidatorTest : AppRequestValidatorTest(ConfirmIdentityFactory) {

    override fun validateClientRequest_shouldFailOnModuleId() {}

    override fun validateClientRequest_shouldFailOnUserId() {}

    @Test
    fun validateClientRequest_shouldFailOnSessionId() {
        val extractor = ConfirmIdentityFactory.getMockExtractor()
        every { extractor.getSessionId() } returns ""

        assertThrows<InvalidSessionIdException> {
            ConfirmIdentityFactory.getValidator(extractor).validateClientRequest()
        }
    }

    @Test
    fun validateClientRequest_shouldFailOnSelectedGuid() {
        val extractor = ConfirmIdentityFactory.getMockExtractor()
        every { extractor.getSelectedGuid() } returns ""

        assertThrows<InvalidSelectedIdException> {
            ConfirmIdentityFactory.getValidator(extractor).validateClientRequest()
        }
    }
}
