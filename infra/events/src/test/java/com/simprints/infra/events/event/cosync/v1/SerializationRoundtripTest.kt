package com.simprints.infra.events.event.cosync.v1

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvents
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.subject.FaceReference
import com.simprints.infra.events.event.domain.models.subject.FaceTemplate
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SerializationRoundtripTest {

    private val objectMapper = ObjectMapper().apply {
        // Register Kotlin module for proper Kotlin data class support
        registerModule(KotlinModule.Builder().build())

        // Configure to ignore unknown properties (like "className" in TokenizableStringV1)
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val module = SimpleModule().apply {
            addSerializer(TokenizableStringV1::class.java, TokenizableStringV1Serializer())
            addDeserializer(TokenizableStringV1::class.java, TokenizableStringV1Deserializer())
            addDeserializer(CoSyncEnrolmentRecordCreationEventV1::class.java, CoSyncEnrolmentRecordCreationEventV1Deserializer())
        }
        registerModule(module)
    }

    @Test
    fun `serialize V1 and deserialize V1 preserves data`() {
        // Given: internal event converted to V1
        val internalEvent = createInternalEvent()
        val internalWrapper = EnrolmentRecordEvents(listOf(internalEvent))
        val v1Wrapper = internalWrapper.toCoSyncV1()

        // When: serialize to JSON and deserialize back
        val json = objectMapper.writeValueAsString(v1Wrapper)
        val deserialized = objectMapper.readValue(json, CoSyncEnrolmentRecordEventsV1::class.java)

        // Then: data is preserved
        assertEquals("1.0", deserialized.schemaVersion)
        assertEquals(1, deserialized.events.size)
        val event = deserialized.events.first() as CoSyncEnrolmentRecordCreationEventV1
        assertEquals(internalEvent.id, event.id)
        assertEquals(internalEvent.payload.subjectId, event.payload.subjectId)
    }

    @Test
    fun `serialized JSON contains schemaVersion field`() {
        // Given: internal event converted to V1
        val internalEvent = createInternalEvent()
        val internalWrapper = EnrolmentRecordEvents(listOf(internalEvent))
        val v1Wrapper = internalWrapper.toCoSyncV1()

        // When: serialize to JSON
        val json = objectMapper.writeValueAsString(v1Wrapper)

        // Then: JSON contains schemaVersion
        assertTrue(json.contains("\"schemaVersion\":\"1.0\""))
    }

    @Test
    fun `old format JSON without schemaVersion can be deserialized`() {
        // Given: old format JSON without schemaVersion
        val oldFormatJson = """
        {
            "events": [
                {
                    "type": "EnrolmentRecordCreation",
                    "id": "event-1",
                    "payload": {
                        "subjectId": "subject-1",
                        "projectId": "project-1",
                        "moduleId": "module-1",
                        "attendantId": "attendant-1",
                        "biometricReferences": []
                    }
                }
            ]
        }
        """.trimIndent()

        // When: deserialize
        val deserialized = objectMapper.readValue(oldFormatJson, CoSyncEnrolmentRecordEventsV1::class.java)

        // Then: successfully parsed with null schemaVersion
        assertNotNull(deserialized)
        assertEquals(1, deserialized.events.size)
        val event = deserialized.events.first() as CoSyncEnrolmentRecordCreationEventV1
        assertEquals("event-1", event.id)
        assertEquals("subject-1", event.payload.subjectId)
    }

    @Test
    fun `old format with plain string moduleId and attendantId can be deserialized`() {
        // Given: old format with plain strings (pre-TokenizableString)
        val oldFormatJson = """
        {
            "events": [
                {
                    "type": "EnrolmentRecordCreation",
                    "id": "event-1",
                    "payload": {
                        "subjectId": "subject-1",
                        "projectId": "project-1",
                        "moduleId": "plain-module-id",
                        "attendantId": "plain-attendant-id",
                        "biometricReferences": []
                    }
                }
            ]
        }
        """.trimIndent()

        // When: deserialize
        val deserialized = objectMapper.readValue(oldFormatJson, CoSyncEnrolmentRecordEventsV1::class.java)

        // Then: successfully parsed with TokenizableStringV1.Raw
        val event = deserialized.events.first() as CoSyncEnrolmentRecordCreationEventV1
        assertEquals(TokenizableStringV1.Raw("plain-module-id"), event.payload.moduleId)
        assertEquals(TokenizableStringV1.Raw("plain-attendant-id"), event.payload.attendantId)
    }

    @Test
    fun `new format with TokenizableStringV1 can be deserialized`() {
        // Given: new format with TokenizableStringV1 objects
        val newFormatJson = """
        {
            "schemaVersion": "1.0",
            "events": [
                {
                    "type": "EnrolmentRecordCreation",
                    "id": "event-1",
                    "payload": {
                        "subjectId": "subject-1",
                        "projectId": "project-1",
                        "moduleId": {
                            "className": "TokenizableString.Tokenized",
                            "value": "encrypted-module"
                        },
                        "attendantId": {
                            "className": "TokenizableString.Raw",
                            "value": "raw-attendant"
                        },
                        "biometricReferences": []
                    }
                }
            ]
        }
        """.trimIndent()

        // When: deserialize
        val deserialized = objectMapper.readValue(newFormatJson, CoSyncEnrolmentRecordEventsV1::class.java)

        // Then: successfully parsed with correct TokenizableStringV1 types
        val event = deserialized.events.first() as CoSyncEnrolmentRecordCreationEventV1
        assertEquals(TokenizableStringV1.Tokenized("encrypted-module"), event.payload.moduleId)
        assertEquals(TokenizableStringV1.Raw("raw-attendant"), event.payload.attendantId)
    }

    @Test
    fun `roundtrip through domain conversion preserves data`() {
        // Given: internal event
        val internalEvent = createInternalEvent()
        val internalWrapper = EnrolmentRecordEvents(listOf(internalEvent))

        // When: convert to V1, serialize, deserialize, convert back to domain
        val v1Wrapper = internalWrapper.toCoSyncV1()
        val json = objectMapper.writeValueAsString(v1Wrapper)
        val deserializedV1 = objectMapper.readValue(json, CoSyncEnrolmentRecordEventsV1::class.java)
        val domainWrapper = deserializedV1.toDomain()

        // Then: data is preserved
        val domainEvent = domainWrapper.events.first() as EnrolmentRecordCreationEvent
        assertEquals(internalEvent.id, domainEvent.id)
        assertEquals(internalEvent.payload.subjectId, domainEvent.payload.subjectId)
        assertEquals(internalEvent.payload.projectId, domainEvent.payload.projectId)
        assertEquals(internalEvent.payload.moduleId, domainEvent.payload.moduleId)
        assertEquals(internalEvent.payload.attendantId, domainEvent.payload.attendantId)
        assertEquals(1, domainEvent.payload.biometricReferences.size)
        assertEquals(1, domainEvent.payload.externalCredentials.size)
    }

    private fun createInternalEvent() = EnrolmentRecordCreationEvent(
        id = "event-1",
        payload = EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload(
            subjectId = "subject-1",
            projectId = "project-1",
            moduleId = TokenizableString.Raw("module-1"),
            attendantId = TokenizableString.Tokenized("encrypted-attendant-1"),
            biometricReferences = listOf(
                FaceReference(
                    id = "face-1",
                    templates = listOf(FaceTemplate("template-1")),
                    format = "RANK_ONE",
                    metadata = mapOf("quality" to "high"),
                ),
            ),
            externalCredentials = listOf(
                ExternalCredential(
                    id = "cred-1",
                    value = TokenizableString.Tokenized("encrypted-value"),
                    subjectId = "subject-1",
                    type = ExternalCredentialType.NHISCard,
                ),
            ),
        ),
    )
}
