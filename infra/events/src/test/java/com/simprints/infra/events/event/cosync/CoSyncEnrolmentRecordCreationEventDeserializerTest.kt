package com.simprints.infra.events.event.cosync

import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.events.event.domain.models.subject.BiometricReference
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals

class CoSyncEnrolmentRecordCreationEventDeserializerTest {
    private val deserializer = CoSyncEnrolmentRecordCreationEventDeserializer()
    private val objectMapper = ObjectMapper()

    @Test
    fun `deserialize handles old format with plain strings`() {
        val json = JSON_TEMPLATE.format(PLAIN_MODULE, PLAIN_ATTENDANT)
        val parser = objectMapper.createParser(json)
        val context = mockk<DeserializationContext>()
        every {
            context.readTreeAsValue<List<BiometricReference>>(
                any<JsonNode>(),
                any<JavaType>(),
            )
        } returns emptyList()

        val result = deserializer.deserialize(parser, context)

        assertEquals(EVENT_ID, result.id)
        assertEquals(SUBJECT_ID, result.payload.subjectId)
        assertEquals(PROJECT_ID, result.payload.projectId)
        assertEquals(TokenizableString.Raw(MODULE_ID), result.payload.moduleId)
        assertEquals(TokenizableString.Raw(ATTENDANT_ID), result.payload.attendantId)
        assertEquals(emptyList<BiometricReference>(), result.payload.biometricReferences)
    }

    @Test
    fun `deserialize handles new format with TokenizableString`() {
        val json = JSON_TEMPLATE.format(TOKENIZED_MODULE, RAW_ATTENDANT)
        val parser = objectMapper.createParser(json)
        val context = mockk<DeserializationContext>()
        every {
            context.readTreeAsValue(any(), TokenizableString::class.java)
        } returns TokenizableString.Tokenized(ENCRYPTED_MODULE) andThen TokenizableString.Raw(UNENCRYPTED_ATTENDANT)
        every {
            context.readTreeAsValue<List<BiometricReference>>(
                any<JsonNode>(),
                any<JavaType>(),
            )
        } returns emptyList()

        val result = deserializer.deserialize(parser, context)

        assertEquals(EVENT_ID, result.id)
        assertEquals(SUBJECT_ID, result.payload.subjectId)
        assertEquals(PROJECT_ID, result.payload.projectId)
        assertEquals(TokenizableString.Tokenized(ENCRYPTED_MODULE), result.payload.moduleId)
        assertEquals(TokenizableString.Raw(UNENCRYPTED_ATTENDANT), result.payload.attendantId)
        assertEquals(emptyList<BiometricReference>(), result.payload.biometricReferences)
    }

    @Test
    fun `deserialize handles new format with TokenizableString but without explicit class`() {
        val json = JSON_TEMPLATE.format(TOKENIZED_MODULE_NO_CLASS, RAW_ATTENDANT_NO_CLASS)
        val parser = objectMapper.createParser(json)
        val context = mockk<DeserializationContext>()
        every {
            context.readTreeAsValue(any(), TokenizableString::class.java)
        } returns TokenizableString.Raw(ENCRYPTED_MODULE) andThen TokenizableString.Raw(UNENCRYPTED_ATTENDANT)
        every {
            context.readTreeAsValue<List<BiometricReference>>(
                any<JsonNode>(),
                any<JavaType>(),
            )
        } returns emptyList()

        val result = deserializer.deserialize(parser, context)

        assertEquals(EVENT_ID, result.id)
        assertEquals(SUBJECT_ID, result.payload.subjectId)
        assertEquals(PROJECT_ID, result.payload.projectId)
        assertEquals(TokenizableString.Raw(ENCRYPTED_MODULE), result.payload.moduleId)
        assertEquals(TokenizableString.Raw(UNENCRYPTED_ATTENDANT), result.payload.attendantId)
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
