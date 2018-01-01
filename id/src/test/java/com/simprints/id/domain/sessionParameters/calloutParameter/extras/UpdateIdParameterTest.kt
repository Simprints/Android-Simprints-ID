package com.simprints.id.domain.sessionParameters.calloutParameter.extras

import com.simprints.id.domain.callout.CalloutAction
import com.simprints.id.domain.callout.CalloutParameter
import com.simprints.id.domain.callout.CalloutParameters
import com.simprints.id.domain.sessionParameters.calloutParameter.mockTypeParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.testUtils.assertThrows
import com.simprints.libsimprints.Constants.SIMPRINTS_UPDATE_GUID
import org.junit.Assert.assertEquals
import org.junit.Test


class UpdateIdParameterTest {

    private val updateTypeParam = mockTypeParameter(CalloutAction.UPDATE)
    private val otherTypeParam = mockTypeParameter(CalloutAction.VERIFY)

    private val validUpdateId: String = "6dfd1cee-0fe0-4894-9518-ea39e26d9bec"
    private val invalidUpdateId: String = "invalidUpdateId"

    private val validUpdateIdParam = CalloutParameter(SIMPRINTS_UPDATE_GUID, validUpdateId)
    private val invalidUpdateIdParam = CalloutParameter(SIMPRINTS_UPDATE_GUID, invalidUpdateId)

    private val emptyCalloutParameters = CalloutParameters(emptySet())
    private val calloutParametersWithValidUpdateId = CalloutParameters(setOf(validUpdateIdParam))
    private val calloutParametersWithInvalidUpdateId = CalloutParameters(setOf(invalidUpdateIdParam))

    @Test
    fun testValidateSucceedsWhenTypeIsNotUpdateAndUpdateIdIsMissing() {
        val updateIdParam = UpdateIdParameter(emptyCalloutParameters, otherTypeParam)
        updateIdParam.validate()
    }

    @Test
    fun testValidateThrowsErrorWhenTypeIsNotUpdateAndUpdateIdIsPresent() {
        val updateIdParam = UpdateIdParameter(calloutParametersWithValidUpdateId, otherTypeParam)
        val throwable = assertThrows<InvalidCalloutError> {
            updateIdParam.validate()
        }
        assertEquals(ALERT_TYPE.UNEXPECTED_PARAMETER, throwable.alertType)
    }

    @Test
    fun testValidateSucceedsWhenTypeIsUpdateAndUpdateIdIsValid() {
        val updateIdParam = UpdateIdParameter(calloutParametersWithValidUpdateId, updateTypeParam)
        updateIdParam.validate()
    }

    @Test
    fun testValidateThrowsErrorWhenTypeIsUpdateAndUpdateIdIsMissing() {
        val updateIdParam = UpdateIdParameter(emptyCalloutParameters, updateTypeParam)
        val throwable = assertThrows<InvalidCalloutError> {
            updateIdParam.validate()
        }
        assertEquals(ALERT_TYPE.MISSING_UPDATE_GUID, throwable.alertType)
    }

    @Test
    fun testValidateThrowsErrorWhenTypeIsUpdateAndUpdateIdIsInvalid() {
        val updateIdParam = UpdateIdParameter(calloutParametersWithInvalidUpdateId, updateTypeParam)
        val throwable = assertThrows<InvalidCalloutError> {
            updateIdParam.validate()
        }
        assertEquals(ALERT_TYPE.INVALID_UPDATE_GUID, throwable.alertType)
    }

}
