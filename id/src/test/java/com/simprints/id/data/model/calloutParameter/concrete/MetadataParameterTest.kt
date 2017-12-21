package com.simprints.id.data.model.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.data.model.calloutParameter.mockIntent
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.testUtils.assertThrows
import com.simprints.libsimprints.Constants.SIMPRINTS_METADATA
import org.junit.Assert
import org.junit.Test


class MetadataParameterTest {

    private val validMetadata: String = "{\"aKey\":\"watchOutForTheMissingQuote\"}"
    private val invalidMetadata: String = "{\"aKey\":\"watchOutForTheMissingQuote}"

    private val emptyIntent: Intent = mockIntent()
    private val invalidMetadataIntent: Intent = mockIntent(SIMPRINTS_METADATA to invalidMetadata)
    private val validMetadataIntent: Intent = mockIntent(SIMPRINTS_METADATA to validMetadata)

    @Test
    fun testValidateSucceedsWhenValueIsMissing() {
        val metadataParameter = MetadataParameter(emptyIntent)
        metadataParameter.validate()
    }

    @Test
    fun testValidateSucceedsWhenValueIsValid() {
        val metadataParameter = MetadataParameter(validMetadataIntent, { true })
        metadataParameter.validate()
    }

    @Test
    fun testValidateThrowsErrorWhenValueIsInvalid() {
        val metadataParameter = MetadataParameter(invalidMetadataIntent, { false })
        val throwable = assertThrows<InvalidCalloutError> {
            metadataParameter.validate()
        }
        Assert.assertEquals(ALERT_TYPE.INVALID_METADATA, throwable.alertType)
    }

}
