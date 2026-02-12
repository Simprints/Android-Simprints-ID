package com.simprints.infra.events.event.cosync

import com.simprints.core.domain.common.TemplateIdentifier
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.events.event.cosync.v1.CoSyncEnrolmentRecordCreationEvent
import com.simprints.infra.events.event.cosync.v1.CoSyncEnrolmentRecordCreationPayload
import com.simprints.infra.events.event.cosync.v1.CoSyncExternalCredentialType
import com.simprints.infra.events.event.cosync.v1.CoSyncExternalCredential
import com.simprints.infra.events.event.cosync.v1.CoSyncFaceReference
import com.simprints.infra.events.event.cosync.v1.CoSyncFaceTemplate
import com.simprints.infra.events.event.cosync.v1.CoSyncFingerprintReference
import com.simprints.infra.events.event.cosync.v1.CoSyncFingerprintTemplate
import com.simprints.infra.events.event.cosync.v1.CoSyncTemplateIdentifier
import com.simprints.infra.events.event.cosync.v1.CoSyncTokenizableString
import com.simprints.infra.events.event.domain.models.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.FaceReference
import com.simprints.infra.events.event.domain.models.FingerprintReference
import com.simprints.infra.serialization.SimJson
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CoSyncEnrolmentRecordEventsVersioningTest {

    // region Schema version routing

    @Test
    fun `deserialize old JSON without schemaVersion defaults to V1`() {
        val json = buildCoSyncJson(schemaVersion = null)

        val result = SimJson.decodeFromString<CoSyncEnrolmentRecordEvents>(json)

        assertIs<CoSyncEnrolmentRecordEventsV1>(result)
        val domainEvents = result.toDomainEvents()
        assertEquals(1, domainEvents.size)
        assertIs<EnrolmentRecordCreationEvent>(domainEvents.first())
    }

    @Test
    fun `deserialize V1 JSON with schemaVersion parses correctly`() {
        val json = buildCoSyncJson()

        val result = SimJson.decodeFromString<CoSyncEnrolmentRecordEvents>(json)

        assertIs<CoSyncEnrolmentRecordEventsV1>(result)
        assertEquals(CoSyncEnrolmentRecordEventsV1.SCHEMA_VERSION, result.schemaVersion)
        assertEquals(1, result.events.size)
    }

    @Test
    fun `minor version increment deserializes as same major version`() {
        val json = buildCoSyncJson(schemaVersion = "1.3")

        val result = SimJson.decodeFromString<CoSyncEnrolmentRecordEvents>(json)

        assertIs<CoSyncEnrolmentRecordEventsV1>(result)
        assertEquals("1.3", result.schemaVersion)
    }

    @Test
    fun `unknown schemaVersion throws exception`() {
        val json = """{"schemaVersion": "99.0", "events": []}"""

        val exception = assertFailsWith<IllegalArgumentException> {
            SimJson.decodeFromString<CoSyncEnrolmentRecordEvents>(json)
        }
        assertTrue(exception.message!!.contains("99.0"))
    }

    // endregion

    // region Serialization

    @Test
    fun `serialization includes schemaVersion field`() {
        val wrapper = buildSimpleV1Event()

        val json = SimJson.encodeToString<CoSyncEnrolmentRecordEvents>(wrapper)

        assertTrue(json.contains("\"schemaVersion\":\"1.0\""))
    }

    // endregion

    // region Roundtrip

    @Test
    fun `roundtrip preserves basic event data`() {
        val original = CoSyncEnrolmentRecordEventsV1(
            events = listOf(
                CoSyncEnrolmentRecordCreationEvent(
                    id = "event-1",
                    payload = CoSyncEnrolmentRecordCreationPayload(
                        subjectId = "subject-1",
                        projectId = "project-1",
                        moduleId = CoSyncTokenizableString.Raw("module-1"),
                        attendantId = CoSyncTokenizableString.Tokenized("encrypted-attendant"),
                    ),
                ),
            ),
        )

        val event = encodeAndDecodeDomainEvent(original)

        assertEquals("event-1", event.id)
        assertEquals("subject-1", event.payload.subjectId)
        assertEquals("project-1", event.payload.projectId)
        assertEquals(TokenizableString.Raw("module-1"), event.payload.moduleId)
        assertEquals(TokenizableString.Tokenized("encrypted-attendant"), event.payload.attendantId)
    }

    @Test
    fun `roundtrip with face biometric reference preserves data`() {
        val original = CoSyncEnrolmentRecordEventsV1(
            events = listOf(
                CoSyncEnrolmentRecordCreationEvent(
                    id = "event-1",
                    payload = CoSyncEnrolmentRecordCreationPayload(
                        subjectId = "subject-1",
                        projectId = "project-1",
                        moduleId = CoSyncTokenizableString.Raw("module-1"),
                        attendantId = CoSyncTokenizableString.Raw("attendant-1"),
                        biometricReferences = listOf(
                            CoSyncFaceReference(
                                id = "ref-1",
                                templates = listOf(CoSyncFaceTemplate(template = "dGVtcGxhdGU=")),
                                format = "NEC_5",
                            ),
                        ),
                    ),
                ),
            ),
        )

        val event = encodeAndDecodeDomainEvent(original)
        val ref = assertIs<FaceReference>(event.payload.biometricReferences.single())
        assertEquals("ref-1", ref.id)
        assertEquals("NEC_5", ref.format)
        assertEquals("dGVtcGxhdGU=", ref.templates.first().template)
    }

    @Test
    fun `roundtrip with fingerprint biometric reference preserves data`() {
        val original = CoSyncEnrolmentRecordEventsV1(
            events = listOf(
                CoSyncEnrolmentRecordCreationEvent(
                    id = "event-1",
                    payload = CoSyncEnrolmentRecordCreationPayload(
                        subjectId = "subject-1",
                        projectId = "project-1",
                        moduleId = CoSyncTokenizableString.Raw("module-1"),
                        attendantId = CoSyncTokenizableString.Raw("attendant-1"),
                        biometricReferences = listOf(
                            CoSyncFingerprintReference(
                                id = "ref-2",
                                templates = listOf(
                                    CoSyncFingerprintTemplate(
                                        template = "ZmluZ2VycHJpbnQ=",
                                        finger = CoSyncTemplateIdentifier.LEFT_INDEX_FINGER,
                                    ),
                                ),
                                format = "ISO_19794_2",
                            ),
                        ),
                    ),
                ),
            ),
        )

        val event = encodeAndDecodeDomainEvent(original)
        val ref = assertIs<FingerprintReference>(event.payload.biometricReferences.single())
        assertEquals("ref-2", ref.id)
        assertEquals("ISO_19794_2", ref.format)
        assertEquals("ZmluZ2VycHJpbnQ=", ref.templates.first().template)
        assertEquals(TemplateIdentifier.LEFT_INDEX_FINGER, ref.templates.first().finger)
    }

    @Test
    fun `roundtrip with external credentials preserves data`() {
        val original = CoSyncEnrolmentRecordEventsV1(
            events = listOf(
                CoSyncEnrolmentRecordCreationEvent(
                    id = "event-1",
                    payload = CoSyncEnrolmentRecordCreationPayload(
                        subjectId = "subject-1",
                        projectId = "project-1",
                        moduleId = CoSyncTokenizableString.Raw("module-1"),
                        attendantId = CoSyncTokenizableString.Raw("attendant-1"),
                        externalCredentials = listOf(
                            CoSyncExternalCredential(
                                id = "cred-1",
                                value = CoSyncTokenizableString.Tokenized("encrypted-value"),
                                subjectId = "subject-1",
                                type = CoSyncExternalCredentialType.NHISCard,
                            ),
                        ),
                    ),
                ),
            ),
        )

        val event = encodeAndDecodeDomainEvent(original)
        val cred = event.payload.externalCredentials.single()
        assertEquals("cred-1", cred.id)
        assertEquals(TokenizableString.Tokenized("encrypted-value"), cred.value)
        assertEquals("subject-1", cred.subjectId)
    }

    // endregion

    // region JSON deserialization - nested types

    @Test
    fun `deserialize JSON with face biometric reference`() {
        val json = buildCoSyncJson(
            biometricReferences = """[{
                "type": "FACE_REFERENCE",
                "id": "ref-1",
                "templates": [{"template": "dGVtcGxhdGU="}],
                "format": "NEC_5"
            }]""",
        )

        val event = decodeDomainEvent(json)
        val ref = assertIs<FaceReference>(event.payload.biometricReferences.single())
        assertEquals("dGVtcGxhdGU=", ref.templates.first().template)
    }

    @Test
    fun `deserialize JSON with fingerprint biometric reference`() {
        val json = buildCoSyncJson(
            biometricReferences = """[{
                "type": "FINGERPRINT_REFERENCE",
                "id": "ref-2",
                "templates": [{"template": "ZmluZ2VycHJpbnQ=", "finger": "LEFT_INDEX_FINGER"}],
                "format": "ISO_19794_2"
            }]""",
        )

        val event = decodeDomainEvent(json)
        val ref = assertIs<FingerprintReference>(event.payload.biometricReferences.single())
        assertEquals("ZmluZ2VycHJpbnQ=", ref.templates.first().template)
    }

    @Test
    fun `deserialize JSON with external credentials`() {
        val json = buildCoSyncJson(
            externalCredentials = """[{
                "id": "cred-1",
                "value": {"className": "TokenizableString.Tokenized", "value": "encrypted-value"},
                "subjectId": "subject-1",
                "type": "NHISCard"
            }]""",
        )

        val event = decodeDomainEvent(json)
        val cred = event.payload.externalCredentials.single()
        assertEquals("cred-1", cred.id)
        assertEquals(TokenizableString.Tokenized("encrypted-value"), cred.value)
    }

    // endregion

    // region TokenizableString backward compatibility

    @Test
    fun `deserialize event with plain string TokenizableString`() {
        val json = buildCoSyncJson(
            moduleId = "\"module-1\"",
            attendantId = "\"attendant-1\"",
        )

        val event = decodeDomainEvent(json)
        assertEquals(TokenizableString.Raw("module-1"), event.payload.moduleId)
        assertEquals(TokenizableString.Raw("attendant-1"), event.payload.attendantId)
    }

    @Test
    fun `deserialize event with object TokenizableString with className`() {
        val json = buildCoSyncJson(
            moduleId = """{"className": "TokenizableString.Tokenized", "value": "encrypted-module"}""",
            attendantId = """{"className": "TokenizableString.Raw", "value": "raw-attendant"}""",
        )

        val event = decodeDomainEvent(json)
        assertEquals(TokenizableString.Tokenized("encrypted-module"), event.payload.moduleId)
        assertEquals(TokenizableString.Raw("raw-attendant"), event.payload.attendantId)
    }

    @Test
    fun `deserialize event with object TokenizableString without className`() {
        val json = buildCoSyncJson(
            moduleId = """{"value": "encrypted-module"}""",
            attendantId = """{"value": "raw-attendant"}""",
        )

        val event = decodeDomainEvent(json)
        // Missing className defaults to Raw
        assertEquals(TokenizableString.Raw("encrypted-module"), event.payload.moduleId)
        assertEquals(TokenizableString.Raw("raw-attendant"), event.payload.attendantId)
    }

    // endregion

    // region Helpers

    private fun buildCoSyncJson(
        schemaVersion: String? = "1.0",
        moduleId: String = "\"module-1\"",
        attendantId: String = "\"attendant-1\"",
        biometricReferences: String = "[]",
        externalCredentials: String? = null,
    ): String {
        val versionLine = schemaVersion?.let { """"schemaVersion": "$it",""" } ?: ""
        val credentialsLine = externalCredentials?.let { """"externalCredentials": $it,""" } ?: ""
        return """
        {
            $versionLine
            "events": [
                {
                    "type": "EnrolmentRecordCreation",
                    "id": "event-1",
                    "payload": {
                        "subjectId": "subject-1",
                        "projectId": "project-1",
                        "moduleId": $moduleId,
                        "attendantId": $attendantId,
                        $credentialsLine
                        "biometricReferences": $biometricReferences
                    }
                }
            ]
        }
        """.trimIndent()
    }

    private fun buildSimpleV1Event() = CoSyncEnrolmentRecordEventsV1(
        events = listOf(
            CoSyncEnrolmentRecordCreationEvent(
                id = "event-1",
                payload = CoSyncEnrolmentRecordCreationPayload(
                    subjectId = "subject-1",
                    projectId = "project-1",
                    moduleId = CoSyncTokenizableString.Raw("module-1"),
                    attendantId = CoSyncTokenizableString.Raw("attendant-1"),
                ),
            ),
        ),
    )

    private fun decodeDomainEvent(json: String): EnrolmentRecordCreationEvent {
        val result = SimJson.decodeFromString<CoSyncEnrolmentRecordEvents>(json)
        val events = result.toDomainEvents()
        assertEquals(1, events.size)
        return assertIs<EnrolmentRecordCreationEvent>(events.first())
    }

    private fun encodeAndDecodeDomainEvent(
        original: CoSyncEnrolmentRecordEvents,
    ): EnrolmentRecordCreationEvent {
        val json = SimJson.encodeToString<CoSyncEnrolmentRecordEvents>(original)
        return decodeDomainEvent(json)
    }

    // endregion
}
