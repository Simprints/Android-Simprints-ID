package com.simprints.feature.clientapi.mappers.request.validators

import com.simprints.core.tools.utils.randomUUID
import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.requestFactories.VerifyActionFactory
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.every
import org.junit.Assert
import org.junit.Test
import java.util.UUID

internal class VerifyValidatorTest : ActionRequestValidatorTest(VerifyActionFactory) {
    private val mockExtractor = VerifyActionFactory.getMockExtractor()

    @Test
    fun `should fail if no guid to verify`() {
        every { mockExtractor.getVerifyGuid() } returns ""

        assertThrows<InvalidRequestException> {
            VerifyActionFactory.getValidator(mockExtractor).validate()
        }
    }

    @Test
    fun `should fail if not guid `() {
        every { mockExtractor.getVerifyGuid() } returns "Trust me, this is a valid GUID!"

        assertThrows<InvalidRequestException> {
            VerifyActionFactory.getValidator(mockExtractor).validate()
        }
    }

    @Test
    fun `should fail if not valid guid`() {
        // The following UUID is one character short of fitting into the valid pattern
        every { mockExtractor.getVerifyGuid() } returns "123e4567-e89b-12d3-a456-55664244000"

        assertThrows<InvalidRequestException> {
            VerifyActionFactory.getValidator(mockExtractor).validate()
        }
    }

    @Test
    fun `should fail if nil guid`() {
        val nilUuid = UUID(0, 0).toString()
        every { mockExtractor.getVerifyGuid() } returns nilUuid

        assertThrows<InvalidRequestException> {
            VerifyActionFactory.getValidator(mockExtractor).validate()
        }
    }

    @Test
    fun `should not fail if valid guid`() {
        every { mockExtractor.getVerifyGuid() } returns randomUUID()

        try {
            VerifyActionFactory.getValidator(mockExtractor).validate()
        } catch (ex: Exception) {
            Assert.fail()
        }
    }
}
