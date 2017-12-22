package com.simprints.id.domain.calloutValidation.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.domain.calloutValidation.CalloutType
import com.simprints.id.domain.calloutValidation.calloutParameter.mockIntent
import com.simprints.id.domain.calloutValidation.calloutParameter.mockTypeParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.testUtils.assertThrows
import com.simprints.libsimprints.Constants.SIMPRINTS_UPDATE_GUID
import org.junit.Assert.assertEquals
import org.junit.Test


class UpdateIdParameterTest {

    private val updateTypeParam = mockTypeParameter(CalloutType.UPDATE)
    private val otherTypeParam = mockTypeParameter(CalloutType.VERIFY)

    private val validUpdateId: String = "6dfd1cee-0fe0-4894-9518-ea39e26d9bec"
    private val invalidUpdateId: String = "invalidUpdateId"

    private val missingUpdateIdIntent: Intent = mockIntent()
    private val validGuidUpdateIdIntent: Intent = mockIntent(SIMPRINTS_UPDATE_GUID to validUpdateId)
    private val invalidGuidUpdateIdIntent: Intent = mockIntent(SIMPRINTS_UPDATE_GUID to invalidUpdateId)

    @Test
    fun testValidateSucceedsWhenTypeIsNotUpdateAndIntentDoesNotContainUpdateId() {
        val updateIdParam = UpdateIdParameter(missingUpdateIdIntent, otherTypeParam)
        updateIdParam.validate()
    }

    @Test
    fun testValidateThrowsErrorWhenTypeIsNotUpdateAndIntentContainsUpdateId() {
        val updateIdParam = UpdateIdParameter(validGuidUpdateIdIntent, otherTypeParam)
        val throwable = assertThrows<InvalidCalloutError> {
            updateIdParam.validate()
        }
        assertEquals(ALERT_TYPE.UNEXPECTED_PARAMETER, throwable.alertType)
    }

    @Test
    fun testValidateSucceedsWhenTypeIsUpdateAndIntentContainsValidGuidUpdateId() {
        val updateIdParam = UpdateIdParameter(validGuidUpdateIdIntent, updateTypeParam)
        updateIdParam.validate()
    }

    @Test
    fun testValidateThrowsErrorWhenTypeIsUpdateAndDoesNotContainUpdateId() {
        val updateIdParam = UpdateIdParameter(missingUpdateIdIntent, updateTypeParam)
        val throwable = assertThrows<InvalidCalloutError> {
            updateIdParam.validate()
        }
        assertEquals(ALERT_TYPE.MISSING_UPDATE_GUID, throwable.alertType)
    }

    @Test
    fun testValidateThrowsErrorWhenTypeIsUpdateAndIntentContainsInvalidGuidUpdateId() {
        val updateIdParam = UpdateIdParameter(invalidGuidUpdateIdIntent, updateTypeParam)
        val throwable = assertThrows<InvalidCalloutError> {
            updateIdParam.validate()
        }
        assertEquals(ALERT_TYPE.INVALID_UPDATE_GUID, throwable.alertType)
    }

}
