package com.simprints.infra.events.event.cosync

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.events.event.domain.models.subject.BiometricReference
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent

/**
 * Deserializer for [EnrolmentRecordCreationEvent] that reads the JSON node and constructs the
 * [EnrolmentRecordCreationEvent] object.
 * Accounts for past versions of the event where moduleId and attendantId were plain strings.
 */
class CoSyncEnrolmentRecordCreationEventDeserializer :
    StdDeserializer<EnrolmentRecordCreationEvent>(
        EnrolmentRecordCreationEvent::class.java,
    ) {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): EnrolmentRecordCreationEvent {
        val node: JsonNode = p.codec.readTree(p)
        val id = node["id"].asText()
        val payload = node["payload"]

        val subjectId = payload["subjectId"].asText()
        val projectId = payload["projectId"].asText()

        // Try to parse as TokenizableString first, fall back to plain String
        val moduleId = try {
            ctxt.readTreeAsValue(payload["moduleId"], TokenizableString::class.java)
        } catch (_: Exception) {
            TokenizableString.Raw(payload["moduleId"].asText())
        }

        // Try to parse as TokenizableString first, fall back to plain String
        val attendantId = try {
            ctxt.readTreeAsValue(payload["attendantId"], TokenizableString::class.java)
        } catch (_: Exception) {
            TokenizableString.Raw(payload["attendantId"].asText())
        }

        val biometricReferences = ctxt.readTreeAsValue<List<BiometricReference>>(
            payload["biometricReferences"],
            ctxt.typeFactory.constructCollectionType(List::class.java, BiometricReference::class.java),
        )

        return EnrolmentRecordCreationEvent(
            id,
            EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload(
                subjectId,
                projectId,
                moduleId,
                attendantId,
                biometricReferences,
            ),
        )
    }
}
