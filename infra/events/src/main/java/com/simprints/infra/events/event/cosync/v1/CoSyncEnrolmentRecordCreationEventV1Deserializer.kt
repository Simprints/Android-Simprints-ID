package com.simprints.infra.events.event.cosync.v1

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

/**
 * Custom deserializer for CoSyncEnrolmentRecordCreationEventV1 that handles
 * backward compatibility with old JSON formats.
 *
 * This deserializer handles the TokenizableStringV1 migration:
 * - Old format: "moduleId": "plain-string"
 * - Middle format (no className): "moduleId": {"value": "..."}
 * - New format: "moduleId": {"className": "TokenizableString.Tokenized", "value": "..."}
 */
class CoSyncEnrolmentRecordCreationEventV1Deserializer :
    StdDeserializer<CoSyncEnrolmentRecordCreationEventV1>(CoSyncEnrolmentRecordCreationEventV1::class.java) {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): CoSyncEnrolmentRecordCreationEventV1 {
        val node: JsonNode = p.codec.readTree(p)
        val id = node["id"].asText()
        val payload = node["payload"]

        // Parse subjectId and projectId as plain strings
        val subjectId = payload["subjectId"].asText()
        val projectId = payload["projectId"].asText()

        // Parse moduleId - try as TokenizableStringV1 first, fall back to plain string
        val moduleId = try {
            ctxt.readTreeAsValue(payload["moduleId"], TokenizableStringV1::class.java)
        } catch (_: Exception) {
            TokenizableStringV1.Raw(payload["moduleId"].asText())
        }

        // Parse attendantId - try as TokenizableStringV1 first, fall back to plain string
        val attendantId = try {
            ctxt.readTreeAsValue(payload["attendantId"], TokenizableStringV1::class.java)
        } catch (_: Exception) {
            TokenizableStringV1.Raw(payload["attendantId"].asText())
        }

        // Parse biometric references
        val biometricReferences = payload["biometricReferences"]?.let {
            ctxt.readTreeAsValue(
                it,
                ctxt.typeFactory.constructCollectionType(List::class.java, BiometricReferenceV1::class.java),
            ) as List<BiometricReferenceV1>
        } ?: emptyList()

        // Parse external credentials (optional field)
        val externalCredentials = payload["externalCredentials"]?.let {
            ctxt.readTreeAsValue(
                it,
                ctxt.typeFactory.constructCollectionType(List::class.java, ExternalCredentialV1::class.java),
            ) as List<ExternalCredentialV1>
        }

        return CoSyncEnrolmentRecordCreationEventV1(
            id = id,
            payload = CoSyncEnrolmentRecordCreationPayloadV1(
                subjectId = subjectId,
                projectId = projectId,
                moduleId = moduleId,
                attendantId = attendantId,
                biometricReferences = biometricReferences,
                externalCredentials = externalCredentials,
            ),
        )
    }
}
