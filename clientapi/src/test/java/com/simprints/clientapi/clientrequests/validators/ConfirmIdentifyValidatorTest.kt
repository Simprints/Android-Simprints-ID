package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.exceptions.InvalidSelectedIdException
import com.simprints.clientapi.exceptions.InvalidSessionIdException
import com.simprints.clientapi.requestFactories.ConfirmIdentifyFactory
import com.simprints.testtools.common.syntax.whenever
import org.junit.Assert
import org.junit.Test

class ConfirmIdentifyValidatorTest : AppRequestValidatorTest(ConfirmIdentifyFactory) {

    override fun validateClientRequest_shouldFailOnModuleId() {}

    override fun validateClientRequest_shouldFailOnUserId() {}

    @Test
    fun validateClientRequest_shouldFailOnSessionId() {
        val extractor = ConfirmIdentifyFactory.getMockExtractor()
        whenever(extractor) { getSessionId() } thenReturn ""

        try {
            ConfirmIdentifyFactory.getValidator(extractor).validateClientRequest()
            Assert.fail()
        } catch (ex: Exception) {
            Assert.assertTrue(ex is InvalidSessionIdException)
        }
    }

    @Test
    fun validateClientRequest_shouldFailOnSelectedGuid() {
        val extractor = ConfirmIdentifyFactory.getMockExtractor()
        whenever(extractor) { getSelectedGuid() } thenReturn ""

        try {
            ConfirmIdentifyFactory.getValidator(extractor).validateClientRequest()
            Assert.fail()
        } catch (ex: Exception) {
            Assert.assertTrue(ex is InvalidSelectedIdException)
        }
    }
}
