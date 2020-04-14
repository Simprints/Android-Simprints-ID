package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.exceptions.InvalidVerifyIdException
import com.simprints.clientapi.requestFactories.VerifyRequestFactory
import com.simprints.core.tools.utils.randomUUID
import io.mockk.every
import org.junit.Test
import java.util.*

class VerifyValidatorTest : AppRequestValidatorTest(VerifyRequestFactory) {

    private val mockExtractor = VerifyRequestFactory.getMockExtractor()

    @Test(expected = InvalidVerifyIdException::class)
    fun withEmptyGuid_shouldThrowException() {
        every { mockExtractor.getVerifyGuid() } returns ""

        VerifyRequestFactory.getValidator(mockExtractor).validateClientRequest()
    }

    @Test(expected = InvalidVerifyIdException::class)
    fun withRandomString_shouldThrowException() {
        every { mockExtractor.getVerifyGuid() } returns "Trust me, this is a valid GUID!"

        VerifyRequestFactory.getValidator(mockExtractor).validateClientRequest()
    }

    @Test(expected = InvalidVerifyIdException::class)
    fun withInvalidGuid_shouldThrowException() {
        // The following UUID is one character short of fitting into the valid pattern
        every { mockExtractor.getVerifyGuid() } returns "123e4567-e89b-12d3-a456-55664244000"

        VerifyRequestFactory.getValidator(mockExtractor).validateClientRequest()
    }

    @Test(expected = InvalidVerifyIdException::class)
    fun withNilUuid_shouldThrowException() {
        val nilUuid = UUID(0, 0).toString()
        every { mockExtractor.getVerifyGuid() } returns nilUuid

        VerifyRequestFactory.getValidator(mockExtractor).validateClientRequest()
    }

    @Test
    fun withValidGuid_shouldNotThrowException() {
        every { mockExtractor.getVerifyGuid() } returns randomUUID()

        VerifyRequestFactory.getValidator(mockExtractor).validateClientRequest()
    }

}
