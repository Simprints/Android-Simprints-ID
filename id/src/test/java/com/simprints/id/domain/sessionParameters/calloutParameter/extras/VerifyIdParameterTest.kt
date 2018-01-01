package com.simprints.id.domain.sessionParameters.calloutParameter.extras

import com.simprints.id.domain.callout.CalloutAction
import com.simprints.id.domain.callout.CalloutParameter
import com.simprints.id.domain.callout.CalloutParameters
import com.simprints.id.domain.sessionParameters.calloutParameter.mockTypeParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.testUtils.assertThrows
import com.simprints.libsimprints.Constants
import org.junit.Assert
import org.junit.Test


class VerifyIdParameterTest {

    private val verifyTypeParam = mockTypeParameter(CalloutAction.VERIFY)
    private val otherTypeParam = mockTypeParameter(CalloutAction.UPDATE)

    private val validVerifyId: String = "6dfd1cee-0fe0-4894-9518-ea39e26d9bec"
    private val invalidVerifyId: String = "invalidVerifyId"

    private val validVerifyIdParam = CalloutParameter(Constants.SIMPRINTS_VERIFY_GUID, validVerifyId)
    private val invalidVerifyIdParam = CalloutParameter(Constants.SIMPRINTS_VERIFY_GUID, invalidVerifyId)

    private val emptyCalloutParameters = CalloutParameters(emptySet())
    private val calloutParametersWithValidVerifyId = CalloutParameters(setOf(validVerifyIdParam))
    private val calloutParametersWithInvalidVerifyId = CalloutParameters(setOf(invalidVerifyIdParam))

    @Test
    fun testValidateSucceedsWhenTypeIsNotVerifyAndVerifyIdIsMissing() {
        val verifyIdParam = VerifyIdParameter(emptyCalloutParameters, otherTypeParam)
        verifyIdParam.validate()
    }

    @Test
    fun testValidateThrowsErrorWhenTypeIsNotVerifyAndVerifyIdIsPresent() {
        val verifyIdParam = VerifyIdParameter(calloutParametersWithValidVerifyId, otherTypeParam)
        val throwable = assertThrows<InvalidCalloutError> {
            verifyIdParam.validate()
        }
        Assert.assertEquals(ALERT_TYPE.UNEXPECTED_PARAMETER, throwable.alertType)
    }

    @Test
    fun testValidateSucceedsWhenTypeIsVerifyAndVerifyIdIsValid() {
        val verifyIdParam = VerifyIdParameter(calloutParametersWithValidVerifyId, verifyTypeParam)
        verifyIdParam.validate()
    }

    @Test
    fun testValidateThrowsErrorWhenTypeIsVerifyAndVerifyIdIsMissing() {
        val verifyIdParam = VerifyIdParameter(emptyCalloutParameters, verifyTypeParam)
        val throwable = assertThrows<InvalidCalloutError> {
            verifyIdParam.validate()
        }
        Assert.assertEquals(ALERT_TYPE.MISSING_VERIFY_GUID, throwable.alertType)
    }

    @Test
    fun testValidateThrowsErrorWhenTypeIsVerifyAndVerifyIdIsInvalid() {
        val verifyIdParam = VerifyIdParameter(calloutParametersWithInvalidVerifyId, verifyTypeParam)
        val throwable = assertThrows<InvalidCalloutError> {
            verifyIdParam.validate()
        }
        Assert.assertEquals(ALERT_TYPE.INVALID_VERIFY_GUID, throwable.alertType)
    }

}
