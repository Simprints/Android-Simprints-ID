package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.exceptions.InvalidModuleIdException
import com.simprints.clientapi.exceptions.InvalidProjectIdException
import com.simprints.clientapi.exceptions.InvalidUserIdException
import com.simprints.clientapi.requestFactories.RequestFactory
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito

abstract class ClientRequestValidatorTest(private val mockFactory: RequestFactory) {

    @Test
    fun validateClientRequest_shouldNotFail() {
        mockFactory.getValidator(mockFactory.getMockExtractor()).validateClientRequest()
    }

    @Test
    fun validateClientRequest_shouldFailOnProjectId() {
        val extractor = mockFactory.getMockExtractor()
        Mockito.`when`(extractor.getProjectId()).thenReturn("")
        Mockito.`when`(extractor.getLegacyApiKey()).thenReturn("")

        try {
            mockFactory.getValidator(extractor).validateClientRequest()
            Assert.fail()
        } catch (ex: Exception) {
            Assert.assertTrue(ex is InvalidProjectIdException)
        }
    }

    @Test
    fun validateClientRequest_shouldFailOnUserId() {
        val extractor = mockFactory.getMockExtractor()
        Mockito.`when`(extractor.getUserId()).thenReturn("")

        try {
            mockFactory.getValidator(extractor).validateClientRequest()
            Assert.fail()
        } catch (ex: Exception) {
            Assert.assertTrue(ex is InvalidUserIdException)
        }
    }

    @Test
    fun validateClientRequest_shouldFailOnModuleId() {
        val extractor = mockFactory.getMockExtractor()
        Mockito.`when`(extractor.getModuleId()).thenReturn("")

        try {
            mockFactory.getValidator(extractor).validateClientRequest()
            Assert.fail()
        } catch (ex: Exception) {
            Assert.assertTrue(ex is InvalidModuleIdException)
        }
    }

    @Test
    fun validateClientRequest_shouldSucceedOnEmptyMetadata() {
        val extractor = mockFactory.getMockExtractor()
        Mockito.`when`(extractor.getMetatdata()).thenReturn("")

        try {
            mockFactory.getValidator(extractor).validateClientRequest()
        } catch (ex: Exception) {
            Assert.fail()
        }
    }

}
