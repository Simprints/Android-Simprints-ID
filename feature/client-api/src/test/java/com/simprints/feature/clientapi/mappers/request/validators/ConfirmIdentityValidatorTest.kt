package com.simprints.feature.clientapi.mappers.request.validators

import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.requestFactories.ConfirmIdentityActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.RequestActionFactory
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.every
import org.junit.Test

internal class ConfirmIdentityValidatorTest : ActionRequestValidatorTest(ConfirmIdentityActionFactory) {
    override fun `should fail if no moduleId`() {}

    override fun `should fail with illegal moduleId`() {}

    override fun `should fail if no userId`() {}

    override fun `should fail with illegal metadata`() {}

    @Test
    fun `should fail if no sessionId`() {
        val extractor = ConfirmIdentityActionFactory.getMockExtractor()
        every { extractor.getSessionId() } returns ""

        assertThrows<InvalidRequestException> {
            ConfirmIdentityActionFactory.getValidator(extractor).validate()
        }
    }

    @Test
    fun `should fail if no selectedGuid`() {
        val extractor = ConfirmIdentityActionFactory.getMockExtractor()
        every { extractor.getSelectedGuid() } returns ""

        assertThrows<InvalidRequestException> {
            ConfirmIdentityActionFactory.getValidator(extractor).validate()
        }
    }

    @Test
    fun `should fail if no identification callback in session`() {
        val extractor = ConfirmIdentityActionFactory.getMockExtractor()
        val validator = ConfirmIdentityValidator(extractor, RequestActionFactory.MOCK_SESSION_ID, false)
        assertThrows<InvalidRequestException> {
            validator.validate()
        }
    }

    @Test
    fun `should fail if invalid sessionId`() {
        val extractor = ConfirmIdentityActionFactory.getMockExtractor()
        val validator = ConfirmIdentityValidator(extractor, "anotherSessionID", false)
        assertThrows<InvalidRequestException> {
            validator.validate()
        }
    }
}
