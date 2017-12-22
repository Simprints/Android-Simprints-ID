package com.simprints.id.domain.calloutValidation.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.domain.calloutValidation.calloutParameter.mockIntent
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.testUtils.assertThrows
import com.simprints.libsimprints.Constants
import org.junit.Assert
import org.junit.Test


class UserIdParameterTest {

    private val aUserId: String = "aUserId"

    private val emptyIntent: Intent = mockIntent()
    private val userIdIntent: Intent = mockIntent(Constants.SIMPRINTS_USER_ID to aUserId)

    @Test
    fun testValidateThrowsErrorWhenValueIsMissing() {
        val userIdParameter = UserIdParameter(emptyIntent)
        val throwable = assertThrows<InvalidCalloutError> {
            userIdParameter.validate()
        }
        Assert.assertEquals(ALERT_TYPE.MISSING_USER_ID, throwable.alertType)
    }

    @Test
    fun testValidateSucceedsWhenValueIsNotMissing() {
        val userIdParameter = UserIdParameter(userIdIntent)
        userIdParameter.validate()
    }

}
