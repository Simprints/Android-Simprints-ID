package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.exceptions.InvalidModuleIdException
import com.simprints.clientapi.exceptions.InvalidProjectIdException
import com.simprints.clientapi.exceptions.InvalidUserIdException
import com.simprints.clientapi.requestFactories.RequestFactory
import io.mockk.every
import org.junit.Assert
import org.junit.Test

abstract class AppRequestValidatorTest(private val mockFactory: RequestFactory) {

    @Test
    open fun validateClientRequest_shouldNotFail() {
        mockFactory.getValidator(mockFactory.getMockExtractor()).validateClientRequest()
    }

    @Test
    open fun validateClientRequest_shouldFailOnProjectId() {
        val extractor = mockFactory.getMockExtractor()
        every { extractor.getProjectId() } returns ""

        try {
            mockFactory.getValidator(extractor).validateClientRequest()
            Assert.fail()
        } catch (ex: Exception) {
            Assert.assertTrue(ex is InvalidProjectIdException)
        }
    }

    @Test
    open fun validateClientRequest_shouldFailOnUserId() {
        val extractor = mockFactory.getMockExtractor()
        every { extractor.getUserId() } returns ""

        try {
            mockFactory.getValidator(extractor).validateClientRequest()
            Assert.fail()
        } catch (ex: Exception) {
            Assert.assertTrue(ex is InvalidUserIdException)
        }
    }

    @Test
    open fun validateClientRequest_shouldFailOnModuleId() {
        val extractor = mockFactory.getMockExtractor()
        every { extractor.getModuleId() } returns ""

        try {
            mockFactory.getValidator(extractor).validateClientRequest()
            Assert.fail()
        } catch (ex: Exception) {
            Assert.assertTrue(ex is InvalidModuleIdException)
        }
    }

    @Test
    open fun validateClientRequest_shouldSucceedOnEmptyMetadata() {
        val extractor = mockFactory.getMockExtractor()
        every { extractor.getMetadata() } returns ""

        try {
            mockFactory.getValidator(extractor).validateClientRequest()
        } catch (ex: Exception) {
            Assert.fail()
        }
    }
}
