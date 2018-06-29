package com.simprints.id.session.sessionParameters.validators

import com.google.gson.Gson
import com.simprints.id.shared.assertThrows
import org.junit.Test

class MetadataValidatorTest {

    private val emptyMetadata: String = ""
    private val validMetadata: String = "{\"aKey\":\"watchOutForTheMissingQuote\"}"
    private val invalidMetadata: String = "{\"aKey\":\"watchOutForTheMissingQuote}"

    private val invalidMetadataError = Error()

    private val metadataValidator = MetadataValidator(invalidMetadataError, Gson())

    @Test
    fun testValidateSucceedsOnEmptyMetadata() {
        metadataValidator.validate(emptyMetadata)
    }

    @Test
    fun testValidateSucceedsOnValidNonEmptyMetadata() {
        metadataValidator.validate(validMetadata)
    }

    @Test
    fun testValidateThrowsErrorOnInvalidMetadata() {
        assertThrows(invalidMetadataError) {
            metadataValidator.validate(invalidMetadata)
        }
    }
}
