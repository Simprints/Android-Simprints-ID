package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.exceptions.InvalidVerifyIdException
import com.simprints.clientapi.requestFactories.VerifyRequestFactory
import com.simprints.testtools.common.syntax.whenever
import org.junit.Assert
import org.junit.Test

class VerifyValidatorTest : AppRequestValidatorTest(VerifyRequestFactory) {

    @Test
    fun validateClientRequest_shouldFailOnVerifyGuid() {
        val extractor = VerifyRequestFactory.getMockExtractor()
        whenever(extractor) { getVerifyGuid() } thenReturn ""

        try {
            VerifyRequestFactory.getValidator(extractor).validateClientRequest()
            Assert.fail()
        } catch (ex: Exception) {
            Assert.assertTrue(ex is InvalidVerifyIdException)
        }
    }
}
