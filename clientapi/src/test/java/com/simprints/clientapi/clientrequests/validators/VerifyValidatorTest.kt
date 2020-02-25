package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.exceptions.InvalidVerifyIdException
import com.simprints.clientapi.requestFactories.VerifyRequestFactory
import com.simprints.core.tools.utils.randomUUID
import com.simprints.testtools.common.syntax.whenever
import org.junit.Test
import java.util.*

class VerifyValidatorTest : AppRequestValidatorTest(VerifyRequestFactory) {

    private val mockExtractor = VerifyRequestFactory.getMockExtractor()

    @Test(expected = InvalidVerifyIdException::class)
    fun withEmptyGuid_shouldThrowException() {
        whenever(mockExtractor) { getVerifyGuid() } thenReturn ""

        VerifyRequestFactory.getValidator(mockExtractor).validateClientRequest()
    }
    
    @Test(expected = InvalidVerifyIdException::class)
    fun withRandomString_shouldThrowException() {
        whenever(mockExtractor) { getVerifyGuid() } thenReturn "Trust me, this is a valid GUID!"

        VerifyRequestFactory.getValidator(mockExtractor).validateClientRequest()
    }

    @Test(expected = InvalidVerifyIdException::class)
    fun withInvalidGuid_shouldThrowException() {
        // The following UUID is one character short of fitting into the valid pattern
        whenever(mockExtractor) { getVerifyGuid() } thenReturn "123e4567-e89b-12d3-a456-55664244000"

        VerifyRequestFactory.getValidator(mockExtractor).validateClientRequest()
    }

    @Test(expected = InvalidVerifyIdException::class)
    fun withNilUuid_shouldThrowException() {
        val nilUuid = UUID(0, 0).toString()
        whenever(mockExtractor) { getVerifyGuid() } thenReturn nilUuid

        VerifyRequestFactory.getValidator(mockExtractor).validateClientRequest()
    }

    @Test
    fun withValidGuid_shouldNotThrowException() {
        whenever(mockExtractor) { getVerifyGuid() } thenReturn randomUUID()

        VerifyRequestFactory.getValidator(mockExtractor).validateClientRequest()
    }

}
