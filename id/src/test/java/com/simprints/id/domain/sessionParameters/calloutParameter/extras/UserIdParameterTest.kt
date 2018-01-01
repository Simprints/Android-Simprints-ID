package com.simprints.id.domain.sessionParameters.calloutParameter.extras

import com.simprints.id.domain.callout.CalloutParameter
import com.simprints.id.domain.callout.CalloutParameters
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.testUtils.assertThrows
import com.simprints.libsimprints.Constants.SIMPRINTS_USER_ID
import org.junit.Assert
import org.junit.Test


class UserIdParameterTest {

    private val userId = CalloutParameter(SIMPRINTS_USER_ID, "userId")

    private val emptyCalloutParameters = CalloutParameters(emptySet())
    private val calloutParametersWithUserId = CalloutParameters(setOf(userId))

    @Test
    fun testValidateThrowsErrorWhenValueIsMissing() {
        val userIdParameter = UserIdParameter(emptyCalloutParameters)
        val throwable = assertThrows<InvalidCalloutError> {
            userIdParameter.validate()
        }
        Assert.assertEquals(ALERT_TYPE.MISSING_USER_ID, throwable.alertType)
    }

    @Test
    fun testValidateSucceedsWhenValueIsNotMissing() {
        val userIdParameter = UserIdParameter(calloutParametersWithUserId)
        userIdParameter.validate()
    }

}
