package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.exceptions.InvalidVerifyIdException
import com.simprints.clientapi.requestFactories.MockVerifyFactory
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito

class VerifyValidatorTest : ClientRequestValidatorTest(MockVerifyFactory) {

    @Test
    fun validateClientRequest_shouldFailOnVerifyGuid() {
        val extractor = MockVerifyFactory.getValidMockExtractor()
        Mockito.`when`(extractor.getVerifyGuid()).thenReturn("")

        try {
            MockVerifyFactory.getValidator(extractor).validateClientRequest()
            Assert.fail()
        } catch (ex: Exception) {
            Assert.assertTrue(ex is InvalidVerifyIdException)
        }
    }

}
