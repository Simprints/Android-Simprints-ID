package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.exceptions.InvalidVerifyIdException
import com.simprints.clientapi.requestFactories.VerifyRequestFactory
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito

class VerifyValidatorTest : ClientRequestValidatorTest(VerifyRequestFactory) {

    @Test
    fun validateClientRequest_shouldFailOnVerifyGuid() {
        val extractor = VerifyRequestFactory.getMockExtractor()
        Mockito.`when`(extractor.getVerifyGuid()).thenReturn("")

        try {
            VerifyRequestFactory.getValidator(extractor).validateClientRequest()
            Assert.fail()
        } catch (ex: Exception) {
            Assert.assertTrue(ex is InvalidVerifyIdException)
        }
    }

}
