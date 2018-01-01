package com.simprints.id.domain.sessionParameters.calloutParameter.extras

import com.simprints.id.domain.callout.CalloutParameter
import com.simprints.id.domain.callout.CalloutParameters
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.testUtils.assertThrows
import com.simprints.libsimprints.Constants.SIMPRINTS_METADATA
import org.junit.Assert
import org.junit.Test


class MetadataParameterTest {

    private val validMetadata: String = "{\"aKey\":\"watchOutForTheMissingQuote\"}"
    private val invalidMetadata: String = "{\"aKey\":\"watchOutForTheMissingQuote}"

    private val validMetadataParam = CalloutParameter(SIMPRINTS_METADATA, validMetadata)
    private val invalidMetadataParam = CalloutParameter(SIMPRINTS_METADATA, invalidMetadata)

    private val emptyCalloutParameters = CalloutParameters(emptySet())
    private val calloutParametersWithValidMetadata = CalloutParameters(setOf(validMetadataParam))
    private val calloutParametersWithInvalidMetadata = CalloutParameters(setOf(invalidMetadataParam))

    @Test
    fun testValidateSucceedsWhenValueIsMissing() {
        val metadataParameter = MetadataParameter(emptyCalloutParameters)
        metadataParameter.validate()
    }

    @Test
    fun testValidateSucceedsWhenValueIsValid() {
        val metadataParameter = MetadataParameter(calloutParametersWithValidMetadata, { true })
        metadataParameter.validate()
    }

    @Test
    fun testValidateThrowsErrorWhenValueIsInvalid() {
        val metadataParameter = MetadataParameter(calloutParametersWithInvalidMetadata, { false })
        val throwable = assertThrows<InvalidCalloutError> {
            metadataParameter.validate()
        }
        Assert.assertEquals(ALERT_TYPE.INVALID_METADATA, throwable.alertType)
    }

}
