package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.exceptions.InvalidVerifyIdException
import com.simprints.clientapi.requestFactories.VerifyRequestFactory
import com.simprints.core.tools.utils.randomUUID
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.every
import org.junit.Assert
import org.junit.Test
import java.util.*

class VerifyValidatorTest : AppRequestValidatorTest(VerifyRequestFactory) {

    private val mockExtractor = VerifyRequestFactory.getMockExtractor()

    @Test
    fun withEmptyGuid_shouldThrowException() {
        every { mockExtractor.getVerifyGuid() } returns ""

        assertThrows<InvalidVerifyIdException> {
            VerifyRequestFactory.getValidator(mockExtractor).validateClientRequest()
        }
    }

    @Test
    fun withRandomString_shouldThrowException() {
        every { mockExtractor.getVerifyGuid() } returns "Trust me, this is a valid GUID!"

        assertThrows<InvalidVerifyIdException> {
            VerifyRequestFactory.getValidator(mockExtractor).validateClientRequest()
        }
    }

    @Test
    fun withInvalidGuid_shouldThrowException() {
        // The following UUID is one character short of fitting into the valid pattern
        every { mockExtractor.getVerifyGuid() } returns "123e4567-e89b-12d3-a456-55664244000"

        assertThrows<InvalidVerifyIdException> {
            VerifyRequestFactory.getValidator(mockExtractor).validateClientRequest()
        }
    }

    @Test
    fun withNilUuid_shouldThrowException() {
        val nilUuid = UUID(0, 0).toString()
        every { mockExtractor.getVerifyGuid() } returns nilUuid

        assertThrows<InvalidVerifyIdException> {
            VerifyRequestFactory.getValidator(mockExtractor).validateClientRequest()
        }
    }

    @Test
    fun withValidGuid_shouldNotThrowException() {
        every { mockExtractor.getVerifyGuid() } returns randomUUID()

        try {
            VerifyRequestFactory.getValidator(mockExtractor).validateClientRequest()
        } catch (ex: Exception) {
            Assert.fail()
        }
    }

}
