package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.exceptions.InvalidModuleIdException
import com.simprints.clientapi.exceptions.InvalidProjectIdException
import com.simprints.clientapi.exceptions.InvalidUserIdException
import com.simprints.clientapi.mockextractors.MockEnrollmentExtractor.getValidEnrollmentExtractorMock
import junit.framework.Assert.assertTrue
import junit.framework.Assert.fail
import org.junit.Test
import org.mockito.Mockito.`when`

class EnrollmentValidatorTest {

    @Test
    fun validateClientRequest_shouldNotFail() {
        EnrollmentValidator(getValidEnrollmentExtractorMock()).validateClientRequest()
    }

    @Test
    fun validateClientRequest_shouldFailOnProjectId() {
        val extractor = getValidEnrollmentExtractorMock()
        `when`(extractor.getProjectId()).thenReturn("")
        `when`(extractor.getLegacyApiKey()).thenReturn("")

        try {
            EnrollmentValidator(extractor).validateClientRequest()
            fail()
        } catch (ex: Exception) {
            assertTrue(ex is InvalidProjectIdException)
        }
    }

    @Test
    fun validateClientRequest_shouldFailOnUserId() {
        val extractor = getValidEnrollmentExtractorMock()
        `when`(extractor.getUserId()).thenReturn("")

        try {
            EnrollmentValidator(extractor).validateClientRequest()
            fail()
        } catch (ex: Exception) {
            assertTrue(ex is InvalidUserIdException)
        }
    }

    @Test
    fun validateClientRequest_shouldFailOnModuleId() {
        val extractor = getValidEnrollmentExtractorMock()
        `when`(extractor.getModuleId()).thenReturn("")

        try {
            EnrollmentValidator(extractor).validateClientRequest()
            fail()
        } catch (ex: Exception) {
            assertTrue(ex is InvalidModuleIdException)
        }
    }

    @Test
    fun validateClientRequest_shouldSucceedOnEmptyMetadata() {
        val extractor = getValidEnrollmentExtractorMock()
        `when`(extractor.getMetatdata()).thenReturn("")

        try {
            EnrollmentValidator(extractor).validateClientRequest()
        } catch (ex: Exception) {
            fail()
        }
    }

}
