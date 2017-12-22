package com.simprints.id.domain.calloutValidation.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.domain.calloutValidation.CalloutType
import com.simprints.id.domain.calloutValidation.calloutParameter.mockIntent
import com.simprints.id.domain.calloutValidation.calloutParameter.mockTypeParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.testUtils.assertThrows
import com.simprints.libsimprints.Constants
import org.junit.Assert
import org.junit.Test


class VerifyIdParameterTest {

    private val verifyTypeParam = mockTypeParameter(CalloutType.VERIFY)
    private val otherTypeParam = mockTypeParameter(CalloutType.UPDATE)

    private val validVerifyId: String = "6dfd1cee-0fe0-4894-9518-ea39e26d9bec"
    private val invalidVerifyId: String = "invalidVerifyId"

    private val missingVerifyIdIntent: Intent = mockIntent()
    private val validGuidVerifyIdIntent: Intent = mockIntent(Constants.SIMPRINTS_VERIFY_GUID to validVerifyId)
    private val invalidGuidVerifyIdIntent: Intent = mockIntent(Constants.SIMPRINTS_VERIFY_GUID to invalidVerifyId)

    @Test
    fun testValidateSucceedsWhenTypeIsNotVerifyAndIntentDoesNotContainVerifyId() {
        val verifyIdParam = VerifyIdParameter(missingVerifyIdIntent, otherTypeParam)
        verifyIdParam.validate()
    }

    @Test
    fun testValidateThrowsErrorWhenTypeIsNotVerifyAndIntentContainsVerifyId() {
        val verifyIdParam = VerifyIdParameter(validGuidVerifyIdIntent, otherTypeParam)
        val throwable = assertThrows<InvalidCalloutError> {
            verifyIdParam.validate()
        }
        Assert.assertEquals(ALERT_TYPE.UNEXPECTED_PARAMETER, throwable.alertType)
    }

    @Test
    fun testValidateSucceedsWhenTypeIsVerifyAndIntentContainsValidGuidVerifyId() {
        val verifyIdParam = VerifyIdParameter(validGuidVerifyIdIntent, verifyTypeParam)
        verifyIdParam.validate()
    }

    @Test
    fun testValidateThrowsErrorWhenTypeIsVerifyAndDoesNotContainVerifyId() {
        val verifyIdParam = VerifyIdParameter(missingVerifyIdIntent, verifyTypeParam)
        val throwable = assertThrows<InvalidCalloutError> {
            verifyIdParam.validate()
        }
        Assert.assertEquals(ALERT_TYPE.MISSING_VERIFY_GUID, throwable.alertType)
    }

    @Test
    fun testValidateThrowsErrorWhenTypeIsVerifyAndIntentContainsInvalidGuidVerifyId() {
        val verifyIdParam = VerifyIdParameter(invalidGuidVerifyIdIntent, verifyTypeParam)
        val throwable = assertThrows<InvalidCalloutError> {
            verifyIdParam.validate()
        }
        Assert.assertEquals(ALERT_TYPE.INVALID_VERIFY_GUID, throwable.alertType)
    }

}
