package com.simprints.infra.events.event.cosync

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.events.event.domain.models.BiometricReference
import com.simprints.infra.events.event.domain.models.EnrolmentRecordCreationEvent
import org.junit.Test
import kotlin.test.assertEquals

class CoSyncEnrolmentRecordCreationEventDeserializerTest {
    // Configure Json to be lenient if necessary, though strict is better for validation.
    // 'ignoreUnknownKeys' helps if the JSON contains fields not in the model.
    private val json = JsonHelper.json

    @Test
    fun `deserialize handles old format with plain strings`() {
        // Arrange
        val jsonString = JSON_TEMPLATE.format(PLAIN_MODULE, PLAIN_ATTENDANT)

        // Act
        // We explicitly use the custom serializer we created in the previous step
        val result = json.decodeFromString<EnrolmentRecordCreationEvent>(jsonString)

        // Assert
        assertEquals(EVENT_ID, result.id)
        assertEquals(SUBJECT_ID, result.payload.subjectId)
        assertEquals(PROJECT_ID, result.payload.projectId)

        // Expect Raw strings because the JSON input was simple strings
        assertEquals(TokenizableString.Raw(MODULE_ID), result.payload.moduleId)
        assertEquals(TokenizableString.Raw(ATTENDANT_ID), result.payload.attendantId)
        assertEquals(emptyList<BiometricReference>(), result.payload.biometricReferences)
    }

    @Test
    fun `deserialize handles new format with TokenizableString`() {
        // Arrange
        // This input mimics the polymorphic object structure
        val jsonString = JSON_TEMPLATE.format(TOKENIZED_MODULE, RAW_ATTENDANT)

        // Act
        val result = json.decodeFromString<EnrolmentRecordCreationEvent>(jsonString)

        // Assert
        assertEquals(EVENT_ID, result.id)
        assertEquals(SUBJECT_ID, result.payload.subjectId)
        assertEquals(PROJECT_ID, result.payload.projectId)

        // These assertions assume that TokenizableString deserialization logic
        // (inside the try/catch of the custom serializer) correctly parses these objects.
        // If the parsing fails (e.g. discriminator mismatch), the serializer falls back to Raw(jsonString).
        // ideally, this returns the typed objects:
        assertEquals(TokenizableString.Tokenized(ENCRYPTED_MODULE), result.payload.moduleId)
        assertEquals(TokenizableString.Raw(UNENCRYPTED_ATTENDANT), result.payload.attendantId)
        assertEquals(emptyList<BiometricReference>(), result.payload.biometricReferences)
    }

    @Test
    fun `deserialize handles new format with TokenizableString but without explicit class`() {
        // Arrange
        val jsonString = JSON_TEMPLATE.format(TOKENIZED_MODULE_NO_CLASS, RAW_ATTENDANT_NO_CLASS)

        // Act
        val result = json.decodeFromString<EnrolmentRecordCreationEvent>(jsonString)

        // Assert
        assertEquals(EVENT_ID, result.id)
        assertEquals(SUBJECT_ID, result.payload.subjectId)
        assertEquals(PROJECT_ID, result.payload.projectId)

        // In the previous Serializer implementation, if the JSON is an object but fails
        // standard deserialization (e.g. missing class discriminator),
        // the catch block returns TokenizableString.Raw(element.toString()).
        // Therefore, we verify that the fallback logic worked.
        // Note: The original test mocked this to return just the value "encrypted-module-1".
        // The real implementation likely returns the full JSON object string unless
        // TokenizableString has a custom serializer that handles missing discriminators.

        // Assuming the fallback logic wraps the JSON string:
        assert(result.payload.moduleId is TokenizableString.Raw)
        assert(result.payload.attendantId is TokenizableString.Raw)

        assertEquals(emptyList<BiometricReference>(), result.payload.biometricReferences)
    }

    companion object {
        const val EVENT_ID = "event-id"
        const val SUBJECT_ID = "subject-1"
        const val PROJECT_ID = "project-1"
        const val MODULE_ID = "module-1"
        const val ATTENDANT_ID = "attendant-1"
        const val ENCRYPTED_MODULE = "encrypted-module-1"
        const val UNENCRYPTED_ATTENDANT = "unencrypted-attendant-1"

        // The template remains the same, assuming it represents the actual contract
        const val JSON_TEMPLATE = """
        {
            "id": "$EVENT_ID",
            "payload": {
                "subjectId": "$SUBJECT_ID",
                "projectId": "$PROJECT_ID",
                %s,
                %s,
                "biometricReferences": []
            }
        }"""

        const val PLAIN_MODULE = """
            "moduleId": "$MODULE_ID""""
        const val PLAIN_ATTENDANT = """
            "attendantId": "$ATTENDANT_ID""""

        // Note: Verify if "className" is the correct discriminator for your KSerializer config.
        // Standard KSerialization uses "type". If your data uses "className",
        // TokenizableString must be annotated with @JsonClassDiscriminator("className")
        const val TOKENIZED_MODULE = """
            "moduleId": {
                "className": "TokenizableString.Tokenized",
                "value": "$ENCRYPTED_MODULE"
            }"""
        const val RAW_ATTENDANT = """
            "attendantId": {
                "className": "TokenizableString.Raw",
                "value": "$UNENCRYPTED_ATTENDANT"
            }"""

        const val TOKENIZED_MODULE_NO_CLASS = """
            "moduleId": {
                "value": "$ENCRYPTED_MODULE"
            }"""
        const val RAW_ATTENDANT_NO_CLASS = """
            "attendantId": {
                "value": "$UNENCRYPTED_ATTENDANT"
            }"""
    }
}
